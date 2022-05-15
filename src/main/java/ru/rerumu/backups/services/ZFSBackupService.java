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
import ru.rerumu.backups.services.impl.S3LoaderImpl;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class ZFSBackupService {

    private final String password;
    private final Logger logger = LoggerFactory.getLogger(ZFSBackupService.class);
    private final ZFSProcessFactory zfsProcessFactory;
    private final int chunkSize;
    private final boolean isLoadS3;
    private final long filePartSize;
    private final FilePartRepository filePartRepository;
    private final ZFSFileSystemRepository zfsFileSystemRepository;
    private final ZFSSnapshotRepository zfsSnapshotRepository;

    public ZFSBackupService(String password,
                            ZFSProcessFactory zfsProcessFactory,
                            int chunkSize,
                            boolean isLoadS3,
                            long filePartSize,
                            FilePartRepository filePartRepository,
                            ZFSFileSystemRepository zfsFileSystemRepository,
                            ZFSSnapshotRepository zfsSnapshotRepository) {
        this.password = password;
        this.zfsProcessFactory = zfsProcessFactory;
        this.chunkSize = chunkSize;
        this.isLoadS3 = isLoadS3;
        this.filePartSize = filePartSize;
        this.filePartRepository = filePartRepository;
        this.zfsFileSystemRepository = zfsFileSystemRepository;
        this.zfsSnapshotRepository = zfsSnapshotRepository;
    }

    private byte[] fillBuffer(BufferedInputStream bufferedInputStream) throws IOException {
        byte[] buf = new byte[0];
        int filled = 0;

        while (true) {
            byte[] readBuf = new byte[chunkSize - filled];
            int len = bufferedInputStream.read(readBuf);
            if (len == -1) {
                return buf;
            }
            byte[] tmp = Arrays.copyOfRange(readBuf, 0, len);
            buf = ArrayUtils.addAll(buf, tmp);
            filled += len;
            if (filled == chunkSize) {
                return buf;
            }
        }
    }

    private void writeToFile(ZFSSend zfsSend, Path path)
            throws IOException,
            CompressorException,
            EncryptException,
            FileHitSizeLimitException,
            ZFSStreamEndedException {

        Cryptor cryptor = new AESCryptor(password);
        Compressor compressor = new GZIPCompressor();

        try (OutputStream outputStream = Files.newOutputStream(path);
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            logger.info(String.format("Writing stream to file '%s'", path.toString()));
            long written = 0;

            while (true) {
                byte[] buf = fillBuffer(zfsSend.getBufferedInputStream());
                if (buf.length == 0){
                    break;
                }
                buf = compressor.compressChunk(buf);
                CryptoMessage cryptoMessage = cryptor.encryptChunk(buf);
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
            while (Files.exists(readyPath)) {
                logger.debug("Last part exists. Waiting 10 seconds before retry");
                Thread.sleep(10000);
            }
        }
    }

    private void sendSingleSnapshot(ZFSSend zfsSend,
                                    S3Loader s3Loader,
                                    String streamMark) throws InterruptedException, CompressorException, IOException, EncryptException {
        int n = 0;
        while (true) {
            Path newFilePath = filePartRepository.createNewFilePath(streamMark, n);
            n++;
            try {
                writeToFile(zfsSend, newFilePath);
            } catch (FileHitSizeLimitException e) {
                processCreatedFile(isLoadS3, s3Loader, newFilePath);
                logger.debug(String.format(
                        "File '%s' processed",
                        newFilePath));
            } catch (ZFSStreamEndedException e) {
                processCreatedFile(isLoadS3, s3Loader, newFilePath);
                logger.debug(String.format(
                        "File '%s' processed",
                        newFilePath));
                logger.info("End of stream. Exiting");
                break;
            }

        }
    }

    private void sendIncrementalSnapshots(Snapshot baseSnapshot, List<Snapshot> incrementalSnapshots, S3Loader s3Loader)
            throws IOException, CompressorException, InterruptedException, EncryptException {
        for (Snapshot incrementalSnapshot : incrementalSnapshots) {
            logger.debug(String.format(
                    "Sending incremental snapshot '%s'. Base snapshot - '%s'",
                    incrementalSnapshot.getFullName(),
                    baseSnapshot.getFullName()));

            String streamMark = escapeSymbols(baseSnapshot.getDataset())
                    + "@" + baseSnapshot.getName()
                    + "__" + escapeSymbols(incrementalSnapshot.getDataset())
                    + "@" + incrementalSnapshot.getName();

            ZFSSend zfsSend = null;
            try {
                zfsSend = zfsProcessFactory.getZFSSendIncremental(baseSnapshot, incrementalSnapshot);
                sendSingleSnapshot(
                        zfsSend,
                        s3Loader,
                        streamMark);
            } catch (Exception e) {
                if (zfsSend != null) {
                    zfsSend.kill();
                }
                throw e;
            } finally {
                if (zfsSend != null) {
                    zfsSend.close();
                }
            }
            baseSnapshot = incrementalSnapshot;
        }
    }

    private String escapeSymbols(String srcString) {
        return srcString.replace('/', '-');
    }

    public void zfsBackupFull(S3Loader s3Loader,
                              String targetSnapshotName,
                              String parentDatasetName) throws
            IOException,
            InterruptedException,
            CompressorException,
            EncryptException, BaseSnapshotNotFoundException, SnapshotNotFoundException {

        List<ZFSFileSystem> zfsFileSystemList = zfsFileSystemRepository.getFilesystemsTreeList(parentDatasetName);

        for (ZFSFileSystem zfsFileSystem : zfsFileSystemList) {
            try {
                Snapshot baseSnapshot = zfsFileSystem.getBaseSnapshot();

                List<Snapshot> incrementalSnapshots = zfsFileSystem.getIncrementalSnapshots(targetSnapshotName);

                logger.debug(String.format("Sending base snapshot '%s'", baseSnapshot.getFullName()));
                String streamMark = escapeSymbols(baseSnapshot.getDataset()) + "@" + baseSnapshot.getName();

                ZFSSend zfsSend = null;
                try {
                    zfsSend = zfsProcessFactory.getZFSSendFull(baseSnapshot);
                    sendSingleSnapshot(
                            zfsSend,
                            s3Loader,
                            streamMark);
                } catch (Exception e) {
                    if (zfsSend != null) {
                        zfsSend.kill();
                    }
                    throw e;
                } finally {
                    if (zfsSend != null) {
                        zfsSend.close();
                    }
                }

                sendIncrementalSnapshots(baseSnapshot, incrementalSnapshots, s3Loader);
            } catch (BaseSnapshotNotFoundException | SnapshotNotFoundException e) {
                logger.error(e.getMessage(), e);
                logger.info(String.format("Skipping filesystem '%s'", zfsFileSystem.getName()));
                continue;
            }

        }
        logger.debug("Sent all filesystems");
    }

    // TODO: Test
    public void zfsBackupIncremental(S3LoaderImpl s3Loader,
                                     String baseSnapshotName,
                                     String targetSnapshotName,
                                     String parentDatasetName) throws
            IOException,
            InterruptedException,
            CompressorException,
            EncryptException, SnapshotNotFoundException {

        List<ZFSFileSystem> zfsFileSystemList = zfsFileSystemRepository.getFilesystemsTreeList(parentDatasetName);

        for (ZFSFileSystem zfsFileSystem : zfsFileSystemList) {
            List<Snapshot> incrementalSnapshots = zfsFileSystem.getIncrementalSnapshots(baseSnapshotName, targetSnapshotName);
            Snapshot baseSnapshot = incrementalSnapshots.remove(0);

            sendIncrementalSnapshots(baseSnapshot, incrementalSnapshots, s3Loader);
        }
    }
}
