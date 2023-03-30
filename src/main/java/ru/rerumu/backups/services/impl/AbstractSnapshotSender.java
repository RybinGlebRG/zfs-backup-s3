package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.LocalBackupRepository;
import ru.rerumu.backups.services.SnapshotSender;
import ru.rerumu.backups.services.ZFSFileWriter;
import ru.rerumu.backups.zfs_api.zfs.ZFSSend;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class AbstractSnapshotSender implements SnapshotSender {
    private final Logger logger = LoggerFactory.getLogger(AbstractSnapshotSender.class);

    protected final LocalBackupRepository localBackupRepository;
    protected final ZFSProcessFactory zfsProcessFactory;
    protected final ZFSFileWriterFactory zfsFileWriterFactory;
    private final Path tempDir;

    public AbstractSnapshotSender(
            LocalBackupRepository localBackupRepository,
            ZFSProcessFactory zfsProcessFactory,
            ZFSFileWriterFactory zfsFileWriterFactory,
            Path tempDir
    ) {
        this.localBackupRepository = localBackupRepository;
        this.zfsProcessFactory = zfsProcessFactory;
        this.zfsFileWriterFactory = zfsFileWriterFactory;
        this.tempDir =tempDir;
    }

    private String escapeSymbols(final String srcString) {
        return srcString.replace('/', '-');
    }

    private void sendStream(
            final ZFSSend zfsSend,
            final String streamMark,
            final String datasetName
    )
            throws InterruptedException,
            CompressorException,
            IOException,
            EncryptException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            S3MissesFileException {
        int n = 0;

        while (true) {
            Path newFilePath = tempDir.resolve(streamMark+".part"+n);
            ZFSFileWriter zfsFileWriter = zfsFileWriterFactory.getZFSFileWriter(newFilePath);
            n++;
            try {
                zfsFileWriter.write(zfsSend.getBufferedInputStream());
            } catch (FileHitSizeLimitException e) {
                localBackupRepository.add(datasetName,newFilePath.getFileName().toString(),newFilePath);
                logger.debug(String.format(
                        "File '%s' processed",
                        newFilePath));
            } catch (ZFSStreamEndedException e) {
                localBackupRepository.add(datasetName,newFilePath.getFileName().toString(),newFilePath);
                logger.debug(String.format(
                        "File '%s' processed",
                        newFilePath));
                logger.info("End of stream. Exiting");
                break;
            }

        }
    }

    protected void sendBaseSnapshot(final Snapshot baseSnapshot)
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
            final Snapshot baseSnapshot,
            final Snapshot incrementalSnapshot
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

    protected ZFSSend getIncrementalProcess(Snapshot baseSnapshot, Snapshot incrementalSnapshot) throws IOException{
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
    public abstract void sendStartingFromIncremental(String datasetName, List<Snapshot> snapshotList)
            throws InterruptedException,
            CompressorException,
            IOException,
            EncryptException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            ExecutionException,
            S3MissesFileException;

}