package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.repositories.RemoteBackupRepository;
import ru.rerumu.backups.services.ZFSFileWriter;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.services.SnapshotSender;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SnapshotSenderImpl implements SnapshotSender {
    private final Logger logger = LoggerFactory.getLogger(SnapshotSenderImpl.class);

    private final FilePartRepository filePartRepository;
    private final RemoteBackupRepository remoteBackupRepository;
    private final ZFSProcessFactory zfsProcessFactory;
    private final ZFSFileWriterFactory zfsFileWriterFactory;
    private final boolean isLoadS3;
    private final List<String> sentFiles = new ArrayList<>();

    public SnapshotSenderImpl(
            FilePartRepository filePartRepository,
            RemoteBackupRepository remoteBackupRepository,
            ZFSProcessFactory zfsProcessFactory,
            ZFSFileWriterFactory zfsFileWriterFactory,
            boolean isLoadS3
    ) {
        this.filePartRepository = filePartRepository;
        this.remoteBackupRepository = remoteBackupRepository;
        this.zfsProcessFactory = zfsProcessFactory;
        this.zfsFileWriterFactory = zfsFileWriterFactory;
        this.isLoadS3 = isLoadS3;
    }

    private String escapeSymbols(String srcString) {
        return srcString.replace('/', '-');
    }

    private void processCreatedFile(boolean isLoadS3,
                                    String datasetName,
                                    Path path)
            throws
            IOException,
            InterruptedException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            S3MissesFileException {
        if (isLoadS3) {
//            s3Loader.upload(datasetName, path);
            remoteBackupRepository.add(datasetName, path);
//            sentFiles.add(path.getFileName().toString());
            filePartRepository.delete(path);
        } else {
            Path readyPath = filePartRepository.markReady(path);
            while (Files.exists(readyPath)) {
                logger.debug("Last part exists. Waiting 1 second before retry");
                Thread.sleep(1000);
            }
        }

    }

    private void sendSingleSnapshot(ZFSSend zfsSend,
                                    String streamMark,
                                    String datasetName,
                                    boolean isLoadS3) throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException {
        int n = 0;
        ZFSFileWriter zfsFileWriter = zfsFileWriterFactory.getZFSFileWriter();
        while (true) {
            Path newFilePath = filePartRepository.createNewFilePath(streamMark, n);
            n++;
            try {
                zfsFileWriter.write(zfsSend.getBufferedInputStream(), newFilePath);
            } catch (FileHitSizeLimitException e) {
                processCreatedFile(isLoadS3, datasetName, newFilePath);
                logger.debug(String.format(
                        "File '%s' processed",
                        newFilePath));
            } catch (ZFSStreamEndedException e) {
                processCreatedFile(isLoadS3, datasetName, newFilePath);
                logger.debug(String.format(
                        "File '%s' processed",
                        newFilePath));
                logger.info("End of stream. Exiting");
                break;
            }

        }
    }

    private void sendBaseSnapshot(Snapshot baseSnapshot, RemoteBackupRepository remoteBackupRepository, boolean isLoadS3)
            throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException, S3MissesFileException {
        String streamMark = escapeSymbols(baseSnapshot.getDataset()) + "@" + baseSnapshot.getName();
        ZFSSend zfsSend = null;
        String datasetName = escapeSymbols(baseSnapshot.getDataset());
        try {
            zfsSend = zfsProcessFactory.getZFSSendFull(baseSnapshot);
            sendSingleSnapshot(
                    zfsSend,
                    streamMark,
                    datasetName,
                    isLoadS3);
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

    }


    private void sendIncrementalSnapshot(Snapshot baseSnapshot, Snapshot incrementalSnapshot, RemoteBackupRepository remoteBackupRepository, boolean isLoadS3)
            throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException, S3MissesFileException {
        String streamMark = escapeSymbols(baseSnapshot.getDataset())
                + "@" + baseSnapshot.getName()
                + "__" + escapeSymbols(incrementalSnapshot.getDataset())
                + "@" + incrementalSnapshot.getName();
        ZFSSend zfsSend = null;
        String datasetName = escapeSymbols(baseSnapshot.getDataset());
        try {
            zfsSend = zfsProcessFactory.getZFSSendIncremental(baseSnapshot, incrementalSnapshot);
            sendSingleSnapshot(
                    zfsSend,
                    streamMark,
                    datasetName,
                    isLoadS3);
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
    }

//    private void checkSent(String datasetName) throws S3MissesFileException {
//        List<String> files = s3Loader.objectsListForDataset(datasetName);
//        logger.info(String.format("Sent files: %s",sentFiles));
//        logger.info(String.format("Found files on S3: %s",files));
//        if (!files.containsAll(sentFiles)){
//            throw new S3MissesFileException();
//        }
//    }

    @Override
    public void sendStartingFromFull(String datasetName, List<Snapshot> snapshotList) throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException, S3MissesFileException {
        boolean isBaseSent = false;

        Snapshot previousSnapshot = null;
        for (Snapshot snapshot : snapshotList) {
            if (!isBaseSent) {
                sendBaseSnapshot(snapshot, remoteBackupRepository, isLoadS3);
                isBaseSent = true;
                previousSnapshot = snapshot;
                continue;
            }
            sendIncrementalSnapshot(previousSnapshot, snapshot, remoteBackupRepository, isLoadS3);
            previousSnapshot = snapshot;

        }
//        checkSent(escapeSymbols(datasetName));
//        sentFiles.clear();
    }

    @Override
    public void sendStartingFromIncremental(String datasetName,List<Snapshot> snapshotList) throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException, S3MissesFileException {
        boolean isBaseSkipped = false;

        Snapshot previousSnapshot = null;
        for (Snapshot snapshot : snapshotList) {
            if (!isBaseSkipped) {
                isBaseSkipped = true;
                previousSnapshot = snapshot;
                continue;
            }
            sendIncrementalSnapshot(previousSnapshot, snapshot, remoteBackupRepository, isLoadS3);
            previousSnapshot = snapshot;

        }
//        checkSent(escapeSymbols(datasetName));
//        sentFiles.clear();
    }

}
