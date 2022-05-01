package ru.rerumu.backups.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.models.CryptoMessage;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.services.impl.AESCryptor;
import ru.rerumu.backups.services.impl.GZIPCompressor;

import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ZFSBackupService {

    private final String password;
    private final Logger logger = LoggerFactory.getLogger(ZFSBackupService.class);
    private final ZFSProcessFactory zfsProcessFactory;
    private final int chunkSize;
    private final boolean isLoadS3;
    private final long filePartSize;
    private final FilePartRepository filePartRepository;

    public ZFSBackupService(String password,
                            ZFSProcessFactory zfsProcessFactory,
                            int chunkSize,
                            boolean isLoadS3,
                            long filePartSize,
                            FilePartRepository filePartRepository) {
        this.password = password;
        this.zfsProcessFactory = zfsProcessFactory;
        this.chunkSize = chunkSize;
        this.isLoadS3 = isLoadS3;
        this.filePartSize = filePartSize;
        this.filePartRepository = filePartRepository;
    }

    private void writeToFile(ZFSSend zfsSend, Path path)
            throws IOException,
            CompressorException,
            EncryptException,
            FileHitSizeLimitException,
            ZFSStreamEndedException {

        Cryptor cryptor = new AESCryptor(password);
        Compressor compressor = new GZIPCompressor();

        try (OutputStream outputStream = filePartRepository.createNewOutputStream(path);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            logger.info(String.format("Writing stream to file '%s'", path.toString()));
            long written = 0;
            int len;
            byte[] buf = new byte[chunkSize];
            while ((len = zfsSend.getBufferedInputStream().read(buf)) >= 0) {
                logger.trace(String.format("Data in buffer: %d bytes", len));
                byte[] tmp = Arrays.copyOfRange(buf, 0, len);
                tmp = compressor.compressChunk(tmp);
                CryptoMessage cryptoMessage = cryptor.encryptChunk(tmp);
                objectOutputStream.writeUnshared(cryptoMessage);
                objectOutputStream.reset();
                written += cryptoMessage.getMessage().length + cryptoMessage.getSalt().length + cryptoMessage.getIv().length;
                logger.trace(String.format("Data written: %d bytes", written));
                if (written >= filePartSize) {
                    logger.debug(String.format("Written (%d bytes) is bigger than filePartSize (%d bytes)", written, filePartSize));
                    throw new FileHitSizeLimitException();
                }
            }
            throw new ZFSStreamEndedException();
        }
    }

    private void processCreatedFile(boolean isLoadS3,
                                    S3Loader s3Loader,
                                    Path path) throws IOException, InterruptedException {
        if (isLoadS3) {
            s3Loader.upload(path);
            filePartRepository.delete(path);
        } else {
            Path readyPath = filePartRepository.markReady(path);
            while (filePartRepository.isExists(readyPath)) {
                logger.debug("Last part exists. Waiting 10 seconds before retry");
                Thread.sleep(10000);
            }
        }
    }

    private void sendSingleSnapshot(ZFSSend zfsSend,
                                    S3Loader s3Loader,
                                    String streamMark) throws InterruptedException, CompressorException, IOException, EncryptException {
        try {
            int n=0;
            while (true) {
                Path newFilePath= filePartRepository.createNewFilePath(streamMark,n);
                n++;
                try {
                    writeToFile(zfsSend, newFilePath);
                } catch (FileHitSizeLimitException e) {
                    processCreatedFile(isLoadS3, s3Loader, newFilePath);
                } catch (ZFSStreamEndedException e) {
                    logger.info("End of stream. Exiting");
                    break;
                }

            }
            zfsSend.close();
        } catch (Exception e) {
            zfsSend.kill();
            throw e;
        }
    }

    // TODO: Test
    public void zfsBackupFull(S3Loader s3Loader,
                              ZFSFileSystemRepository zfsFileSystemRepository,
                              ZFSSnapshotRepository zfsSnapshotRepository,
                              Snapshot targetSnapshot) throws
            IOException,
            InterruptedException,
            CompressorException,
            EncryptException {

        List<ZFSFileSystem> zfsFileSystemList = zfsFileSystemRepository.getFilesystemsTreeList(new ZFSFileSystem(targetSnapshot.getDataset()));

        for (ZFSFileSystem zfsFileSystem : zfsFileSystemList) {

            Snapshot baseSnapshot = zfsSnapshotRepository.getBaseSnapshot(zfsFileSystem);
            logger.debug(String.format("Sending base snapshot '%s'", baseSnapshot.getFullName()));
            String streamMark = baseSnapshot.getDataset() + "_" + baseSnapshot.getName();
            sendSingleSnapshot(
                    zfsProcessFactory.getZFSSendFull(baseSnapshot),
                    s3Loader,
                    streamMark);

            List<Snapshot> incrementalSnapshotList = zfsSnapshotRepository.getAllSnapshotsOrdered(zfsFileSystem);
            for (Snapshot incrementalSnapshot : incrementalSnapshotList) {
                logger.debug(String.format(
                        "Sending incremental snapshot '%s'. Base snapshot - '%s'",
                        incrementalSnapshot.getFullName(),
                        baseSnapshot.getFullName()));

                streamMark = baseSnapshot.getDataset()
                        + "_" + baseSnapshot.getName()
                        + "__" + incrementalSnapshot.getDataset()
                        + "_" + incrementalSnapshot.getName();

                sendSingleSnapshot(
                        zfsProcessFactory.getZFSSendIncremental(baseSnapshot, incrementalSnapshot),
                        s3Loader,
                        streamMark);
                if (incrementalSnapshot.getName().equals(targetSnapshot.getName())) {
                    logger.debug(String.format("Target snapshot '%s' reached", targetSnapshot.getName()));
                    break;
                }
                baseSnapshot = incrementalSnapshot;
            }


        }
    }

    // TODO: Test
    public void zfsBackupIncremental(S3Loader s3Loader,
                                     ZFSFileSystemRepository zfsFileSystemRepository,
                                     ZFSSnapshotRepository zfsSnapshotRepository,
                                     Snapshot targetSnapshot,
                                     Snapshot baseSnapshot) throws
            IOException,
            InterruptedException,
            CompressorException,
            EncryptException {

        if (!targetSnapshot.getDataset().equals(baseSnapshot.getDataset())) {
            throw new IllegalArgumentException();
        }

        List<ZFSFileSystem> zfsFileSystemList = zfsFileSystemRepository.getFilesystemsTreeList(new ZFSFileSystem(targetSnapshot.getDataset()));
        boolean isFoundBaseSnapshot = false;
        for (ZFSFileSystem zfsFileSystem : zfsFileSystemList) {
            List<Snapshot> incrementalSnapshotList = zfsSnapshotRepository.getAllSnapshotsOrdered(zfsFileSystem);
            for (Snapshot incrementalSnapshot : incrementalSnapshotList) {
                if (incrementalSnapshot.getName().equals(baseSnapshot.getName())) {
                    baseSnapshot = incrementalSnapshot;
                    isFoundBaseSnapshot = true;
                }
                if (!isFoundBaseSnapshot) {
                    continue;
                }
                logger.debug(String.format(
                        "Sending incremental snapshot '%s'. Base snapshot - '%s'",
                        incrementalSnapshot.getFullName(),
                        baseSnapshot.getFullName()));

                String streamMark = baseSnapshot.getDataset()
                        + "_" + baseSnapshot.getName()
                        + "__" + incrementalSnapshot.getDataset()
                        + "_" + incrementalSnapshot.getName();

                sendSingleSnapshot(
                        zfsProcessFactory.getZFSSendIncremental(baseSnapshot, incrementalSnapshot),
                        s3Loader,
                        streamMark);
                if (incrementalSnapshot.getName().equals(targetSnapshot.getName())) {
                    logger.debug(String.format("Target snapshot '%s' reached", targetSnapshot.getName()));
                    break;
                }
                baseSnapshot = incrementalSnapshot;
            }


        }
    }
}
