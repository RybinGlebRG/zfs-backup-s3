package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.io.S3Loader;
import ru.rerumu.backups.io.ZFSFileWriter;
import ru.rerumu.backups.io.ZFSFileWriterFactory;
import ru.rerumu.backups.io.impl.ZFSFileWriterFull;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.services.SnapshotSender;
import ru.rerumu.backups.services.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.ZFSSend;

import javax.naming.OperationNotSupportedException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class SnapshotSenderImpl implements SnapshotSender {
    private final Logger logger = LoggerFactory.getLogger(SnapshotSenderImpl.class);

    private final FilePartRepository filePartRepository;
    private final S3Loader s3Loader;
    private final ZFSProcessFactory zfsProcessFactory;
    private final ZFSFileWriterFactory zfsFileWriterFactory;
    private final boolean isLoadS3;

    public SnapshotSenderImpl(
            FilePartRepository filePartRepository,
            S3Loader s3Loader,
            ZFSProcessFactory zfsProcessFactory,
            ZFSFileWriterFactory zfsFileWriterFactory,
            boolean isLoadS3
    ) {
        this.filePartRepository = filePartRepository;
        this.s3Loader = s3Loader;
        this.zfsProcessFactory = zfsProcessFactory;
        this.zfsFileWriterFactory = zfsFileWriterFactory;
        this.isLoadS3 = isLoadS3;
    }

    private String escapeSymbols(String srcString) {
        return srcString.replace('/', '-');
    }

    private void processCreatedFile(boolean isLoadS3,
                                    String datasetName,
                                    Path path) throws IOException, InterruptedException, NoSuchAlgorithmException, IncorrectHashException {
        if (isLoadS3) {
            s3Loader.upload(datasetName, path);
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
                                    boolean isLoadS3) throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException {
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

    @Override
    public void sendBaseSnapshot(Snapshot baseSnapshot, S3Loader s3Loader, boolean isLoadS3)
            throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException {
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

    @Override
    public void sendIncrementalSnapshot(Snapshot baseSnapshot, Snapshot incrementalSnapshot, S3Loader s3Loader, boolean isLoadS3)
            throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException {
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

    // TODO: Test
    @Override
    public void checkSent(List<Snapshot> snapshotList, S3Loader s3Loader) {

    }

    // TODO: Test
    @Override
    public void sendStartingFromFull(List<Snapshot> snapshotList) throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException {
        boolean isBaseSent = false;

        Snapshot previousSnapshot = null;
        for (Snapshot snapshot : snapshotList) {
            if (!isBaseSent) {
                sendBaseSnapshot(snapshot, s3Loader, isLoadS3);
                isBaseSent = true;
                previousSnapshot = snapshot;
                continue;
            }
            sendIncrementalSnapshot(previousSnapshot, snapshot, s3Loader, isLoadS3);
            previousSnapshot = snapshot;

        }
    }

    // TODO: Test
    @Override
    public void sendStartingFromIncremental(List<Snapshot> snapshotList) throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException {
        boolean isBaseSkipped = false;

        Snapshot previousSnapshot = null;
        for (Snapshot snapshot : snapshotList) {
            if (!isBaseSkipped) {
                isBaseSkipped = true;
                previousSnapshot = snapshot;
                continue;
            }
            sendIncrementalSnapshot(previousSnapshot, snapshot, s3Loader, isLoadS3);
            previousSnapshot = snapshot;

        }
    }

}
