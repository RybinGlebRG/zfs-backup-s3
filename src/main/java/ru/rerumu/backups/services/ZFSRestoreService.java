package ru.rerumu.backups.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.repositories.LocalBackupRepository;
import ru.rerumu.backups.repositories.RemoteBackupRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ZFSRestoreService {

    private final Logger logger = LoggerFactory.getLogger(ZFSRestoreService.class);
    private final LocalBackupRepository localBackupRepository;
    private final SnapshotReceiver snapshotReceiver;
    private final boolean isUseAWS;
    private final String datasetName;

    public ZFSRestoreService(LocalBackupRepository localBackupRepository,
                             SnapshotReceiver snapshotReceiver,
                             boolean isUseAWS,
                             String datasetName) {
        this.localBackupRepository = localBackupRepository;
        this.snapshotReceiver = snapshotReceiver;
        this.isUseAWS = isUseAWS;
        this.datasetName = datasetName;
    }

    private void receiveLocal()
            throws FinishedFlagException,
            NoMorePartsException,
            IOException,
            TooManyPartsException,
            IncorrectFilePartNameException,
            CompressorException,
            ClassNotFoundException,
            EncryptException,
            InterruptedException,
            ExecutionException {
        try {
            while (true) {
                try {
                    Path nextPath = localBackupRepository.getNextInputPath();
                    snapshotReceiver.receiveSnapshotPart(nextPath);
                } catch (NoMorePartsException e) {
                    logger.debug("No acceptable files found. Waiting 1 second before retry");
                    Thread.sleep(1000);
                } catch (FinishedFlagException e) {
                    logger.info("Finish flag found. Exiting loop");
                    break;
                }
            }
        } finally {
            snapshotReceiver.finish();
        }
    }

    // TODO: write
    private void receiveRemote()
            throws IOException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            ExecutionException,
            InterruptedException,
            IncorrectFilePartNameException,
            CompressorException,
            ClassNotFoundException,
            EncryptException, FinishedFlagException {

        List<String> datasets = localBackupRepository.getDatasets();

        if (!datasets.contains(datasetName)){
            throw new IllegalArgumentException();
        }

        String currentPart = null;

        try {
            while (true) {
                try {
                    Path path = localBackupRepository.getNextPart(datasetName, currentPart);
                    currentPart = path.getFileName().toString();
                    snapshotReceiver.receiveSnapshotPart(path);
                } catch (FinishedFlagException e) {
                    logger.info("Finish flag found. Exiting loop");
                    break;
                }
            }
        } finally {
            snapshotReceiver.finish();
        }
    }

    public void zfsReceive()
            throws FinishedFlagException,
            NoMorePartsException,
            IOException,
            TooManyPartsException,
            IncorrectFilePartNameException,
            CompressorException,
            ClassNotFoundException,
            EncryptException,
            InterruptedException,
            ExecutionException,
            NoSuchAlgorithmException,
            IncorrectHashException {
        if (isUseAWS){
            receiveRemote();
        } else {
            receiveLocal();
        }
    }
}
