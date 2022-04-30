package ru.rerumu.backups.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.models.CryptoMessage;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.services.impl.AESCryptor;
import ru.rerumu.backups.services.impl.GZIPCompressor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ZFSBackupsService {

    private final String password;
    private final Logger logger = LoggerFactory.getLogger(ZFSBackupsService.class);
    private final ZFSProcessFactory zfsProcessFactory;
    private final int chunkSize;
    private final boolean isLoadS3;
    private final long filePartSize;

    public ZFSBackupsService(String password,
                             ZFSProcessFactory zfsProcessFactory,
                             int chunkSize,
                             boolean isLoadS3,
                             long filePartSize) {
        this.password = password;
        this.zfsProcessFactory = zfsProcessFactory;
        this.chunkSize = chunkSize;
        this.isLoadS3 = isLoadS3;
        this.filePartSize = filePartSize;
    }

    private void writeToFile(FilePartRepository filePartRepository,
                             ZFSSend zfsSend)
            throws IOException,
            CompressorException,
            EncryptException,
            FileHitSizeLimitException,
            ZFSStreamEndedException {

        Cryptor cryptor = new AESCryptor(password);
        Compressor compressor = new GZIPCompressor();

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(filePartRepository.newPart())) {
            logger.info(String.format("Writing stream to file '%s'", filePartRepository.getLastPart().toString()));
            long written = 0;
            int len;
            byte[] buf = new byte[chunkSize];
            byte[] tmp;
            while ((len = zfsSend.getBufferedInputStream().read(buf)) >= 0) {
                logger.trace(String.format("Data in buffer: %d bytes", len));
                tmp = Arrays.copyOfRange(buf, 0, len);
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
                                    FilePartRepository filePartRepository,
                                    S3Loader s3Loader) throws IOException, InterruptedException {
        if (isLoadS3) {
            Path lastPart = filePartRepository.getLastPart();
            s3Loader.upload(lastPart);
            filePartRepository.deleteLastPart();
        } else {
            filePartRepository.markReadyLastPart();
            while (filePartRepository.isLastPartExists()) {
                logger.debug("Last part exists. Waiting 10 seconds before retry");
                Thread.sleep(10000);
            }
        }
    }

    private void sendSingleSnapshot(ZFSSend zfsSend,
                                    FilePartRepository filePartRepository,
                                    S3Loader s3Loader) throws InterruptedException, CompressorException, IOException, EncryptException {
        try {
            while (true) {
                try {
                    writeToFile(filePartRepository, zfsSend);
                } catch (FileHitSizeLimitException e) {
                    processCreatedFile(isLoadS3, filePartRepository, s3Loader);
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
    public void zfsSendFull(FilePartRepository filePartRepository,
                            S3Loader s3Loader,
                            ZFSFileSystemRepository zfsFileSystemRepository,
                            ZFSSnapshotRepository zfsSnapshotRepository,
                            ZFSPool zfsPool,
                            Snapshot targetSnapshot) throws
            IOException,
            InterruptedException,
            CompressorException,
            EncryptException {

        List<ZFSFileSystem> zfsFileSystemList = zfsFileSystemRepository.getAllFilesystems(zfsPool);

        for (ZFSFileSystem zfsFileSystem : zfsFileSystemList) {

            Snapshot baseSnapshot = zfsSnapshotRepository.getBaseSnapshot(zfsFileSystem);
            logger.debug(String.format("Sending base snapshot '%s'", baseSnapshot.getFullName()));
            sendSingleSnapshot(
                    zfsProcessFactory.getZFSSendFull(baseSnapshot),
                    filePartRepository,
                    s3Loader);

            List<Snapshot> incrementalSnapshotList = zfsSnapshotRepository.getAllSnapshotsOrdered(zfsFileSystem);
            for (Snapshot incrementalSnapshot : incrementalSnapshotList) {
                logger.debug(String.format("Sending incremental snapshot '%s'", incrementalSnapshot.getFullName()));
                sendSingleSnapshot(
                        zfsProcessFactory.getZFSSendIncremental(baseSnapshot, incrementalSnapshot),
                        filePartRepository,
                        s3Loader);
                if (incrementalSnapshot.getName().equals(targetSnapshot.getName())) {
                    logger.debug(String.format("Target snapshot '%s' reached", targetSnapshot.getName()));
                    break;
                }
                baseSnapshot = incrementalSnapshot;
            }


        }
    }

    // TODO: Test
    public void zfsSendIncremental(FilePartRepository filePartRepository,
                                   S3Loader s3Loader,
                                   ZFSFileSystemRepository zfsFileSystemRepository,
                                   ZFSSnapshotRepository zfsSnapshotRepository,
                                   ZFSPool zfsPool,
                                   Snapshot targetSnapshot,
                                   Snapshot baseSnapshot) throws
            IOException,
            InterruptedException,
            CompressorException,
            EncryptException {

        List<ZFSFileSystem> zfsFileSystemList = zfsFileSystemRepository.getAllFilesystems(zfsPool);
        boolean isFoundBaseSnaphsot = false;
        for (ZFSFileSystem zfsFileSystem : zfsFileSystemList) {
            List<Snapshot> incrementalSnapshotList = zfsSnapshotRepository.getAllSnapshotsOrdered(zfsFileSystem);
            for (Snapshot incrementalSnapshot : incrementalSnapshotList) {
                if (incrementalSnapshot.getName().equals(baseSnapshot.getName())){
                    baseSnapshot = incrementalSnapshot;
                    isFoundBaseSnaphsot = true;
                }
                if (!isFoundBaseSnaphsot){
                    continue;
                }
                logger.debug(String.format(
                        "Sending incremental snapshot '%s'. Base snapshot - '%s'",
                        incrementalSnapshot.getFullName(),
                        baseSnapshot.getFullName()));
                sendSingleSnapshot(
                        zfsProcessFactory.getZFSSendIncremental(baseSnapshot, incrementalSnapshot),
                        filePartRepository,
                        s3Loader);
                if (incrementalSnapshot.getName().equals(targetSnapshot.getName())) {
                    logger.debug(String.format("Target snapshot '%s' reached", targetSnapshot.getName()));
                    break;
                }
                baseSnapshot = incrementalSnapshot;
            }


        }
    }

    public void zfsReceive(ZFSReceive zfsReceive,
                           FilePartRepository filePartRepository,
                           boolean isDelete) throws
            IOException,
            TooManyPartsException,
            EncryptException,
            CompressorException,
            InterruptedException,
            ClassNotFoundException {
        Cryptor cryptor = new AESCryptor(password);
        Compressor compressor = new GZIPCompressor();

        while (true) {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(filePartRepository.getNextInputStream())) {
                logger.info(String.format("Reading file '%s'", filePartRepository.getLastPart().toString()));
                while (true) {
                    try {
                        logger.trace("Reading object from stream");
                        Object object = objectInputStream.readUnshared();
                        if (object instanceof CryptoMessage) {
                            logger.trace("Trying to cast to CryptoMessage");
                            CryptoMessage cryptoMessage = (CryptoMessage) object;
                            logger.trace("Trying to decrypt chunk");
                            byte[] tmp = cryptor.decryptChunk(cryptoMessage);
                            logger.trace("Trying to decompress chunk");
                            tmp = compressor.decompressChunk(tmp);
                            logger.trace("Writing chunk to stream");
                            zfsReceive.getBufferedOutputStream().write(tmp);
                            logger.trace("End writing chunk to stream");
                        } else {
                            logger.error("Object is not instance of CryptoMessage");
                            throw new IOException();
                        }
                    } catch (EOFException e) {
                        logger.info(String.format("End of file '%s'", filePartRepository.getLastPart().toString()));
                        break;
                    }
                }

            } catch (NoMorePartsException e) {
                logger.debug("No files found. Waiting 10 seconds before retry");
                Thread.sleep(10000);
                continue;
            } catch (FinishedFlagException e) {
                logger.info("Finish flag found. Exiting loop");
                break;
            }

            if (isDelete) {
                filePartRepository.deleteLastPart();
            } else {
                filePartRepository.markReceivedLastPart();
            }
        }

        zfsReceive.close();
    }
}
