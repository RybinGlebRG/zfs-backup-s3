package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.RemoteBackupRepository;
import ru.rerumu.backups.services.SnapshotSender;
import ru.rerumu.backups.services.ZFSFileWriter;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class AbstractSnapshotSender implements SnapshotSender {
    private final Logger logger = LoggerFactory.getLogger(AbstractSnapshotSender.class);

    protected final FilePartRepository filePartRepository;
    protected final RemoteBackupRepository remoteBackupRepository;
    protected final ZFSProcessFactory zfsProcessFactory;
    protected final ZFSFileWriterFactory zfsFileWriterFactory;
    protected final boolean isLoadS3;

    public AbstractSnapshotSender(
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

    protected String escapeSymbols(String srcString) {
        return srcString.replace('/', '-');
    }

    protected void processCreatedFile(String datasetName,
                                      Path path) throws IOException, InterruptedException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException {
        if (isLoadS3) {
            remoteBackupRepository.add(datasetName, path);
            filePartRepository.delete(path);
        } else {
            Path readyPath = filePartRepository.markReady(path);
            while (Files.exists(readyPath)) {
                logger.debug("Last part exists. Waiting 1 second before retry");
                Thread.sleep(1000);
            }
        }

    }

    protected void sendStream(ZFSSend zfsSend,
                              String streamMark,
                              String datasetName) throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException {
        int n = 0;
        ZFSFileWriter zfsFileWriter = zfsFileWriterFactory.getZFSFileWriter();
        while (true) {
            Path newFilePath = filePartRepository.createNewFilePath(streamMark, n);
            n++;
            try {
                zfsFileWriter.write(zfsSend.getBufferedInputStream(), newFilePath);
            } catch (FileHitSizeLimitException e) {
                processCreatedFile(datasetName, newFilePath);
                logger.debug(String.format(
                        "File '%s' processed",
                        newFilePath));
            } catch (ZFSStreamEndedException e) {
                processCreatedFile(datasetName, newFilePath);
                logger.debug(String.format(
                        "File '%s' processed",
                        newFilePath));
                logger.info("End of stream. Exiting");
                break;
            }

        }
    }

    protected void sendBaseSnapshot(Snapshot baseSnapshot)
            throws InterruptedException,
            CompressorException,
            IOException,
            EncryptException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            ExecutionException,
            S3MissesFileException {
        String streamMark = escapeSymbols(baseSnapshot.getDataset()) + "@" + baseSnapshot.getName();
        ZFSSend zfsSend = null;
        String datasetName = escapeSymbols(baseSnapshot.getDataset());
        try {
            zfsSend = zfsProcessFactory.getZFSSendFull(baseSnapshot);
            sendStream(
                    zfsSend,
                    streamMark,
                    datasetName);
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


    protected void sendIncrementalSnapshot(
            Snapshot baseSnapshot,
            Snapshot incrementalSnapshot
    )
            throws InterruptedException,
            CompressorException,
            IOException,
            EncryptException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            ExecutionException,
            S3MissesFileException {
        String streamMark = escapeSymbols(baseSnapshot.getDataset())
                + "@" + baseSnapshot.getName()
                + "__" + escapeSymbols(incrementalSnapshot.getDataset())
                + "@" + incrementalSnapshot.getName();
        ZFSSend zfsSend = null;
        String datasetName = escapeSymbols(baseSnapshot.getDataset());
        try {
            zfsSend = getIncrementalProcess(baseSnapshot, incrementalSnapshot);
            sendStream(
                    zfsSend,
                    streamMark,
                    datasetName);
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

    protected ZFSSend getIncrementalProcess(Snapshot baseSnapshot, Snapshot incrementalSnapshot) throws IOException {
        return zfsProcessFactory.getZFSSendIncremental(baseSnapshot, incrementalSnapshot);
    }

    @Override
    public abstract void sendStartingFromFull(String datasetName, List<Snapshot> snapshotList)
            throws InterruptedException,
            CompressorException,
            IOException,
            EncryptException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            ExecutionException,
            S3MissesFileException;

    @Override
    public abstract void sendStartingFromIncremental(String datasetName,List<Snapshot> snapshotList)
            throws InterruptedException,
            CompressorException,
            IOException,
            EncryptException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            ExecutionException,
            S3MissesFileException;

}