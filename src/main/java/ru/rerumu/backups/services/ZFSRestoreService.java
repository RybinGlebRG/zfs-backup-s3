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
    private final String datasetName;

    public ZFSRestoreService(LocalBackupRepository localBackupRepository,
                             SnapshotReceiver snapshotReceiver,
                             String datasetName) {
        this.localBackupRepository = localBackupRepository;
        this.snapshotReceiver = snapshotReceiver;
        this.datasetName = datasetName;
    }

    // TODO: write
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
}
