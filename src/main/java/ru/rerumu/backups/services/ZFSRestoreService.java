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
    private final List<String> datasetList;

    public ZFSRestoreService(LocalBackupRepository localBackupRepository,
                             SnapshotReceiver snapshotReceiver,
                             List<String> datasetList) {
        this.localBackupRepository = localBackupRepository;
        this.snapshotReceiver = snapshotReceiver;
        this.datasetList = datasetList;
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
            IncorrectHashException, NoPartFoundException {
        List<String> datasets = localBackupRepository.getDatasets();

        for (String datasetName : datasetList) {
            if (!datasets.contains(datasetName)) {
                throw new IllegalArgumentException();
            }
        }
        try {
            for (String datasetName : datasetList) {
                for (String partName : localBackupRepository.getParts(datasetName)) {
                    Path path = localBackupRepository.getPart(datasetName, partName);
                    snapshotReceiver.receiveSnapshotPart(path);
                }
            }
        } finally {
            snapshotReceiver.finish();
        }
    }
}
