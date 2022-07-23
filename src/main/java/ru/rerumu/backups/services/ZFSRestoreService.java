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
            IncorrectHashException {
        List<String> datasets = localBackupRepository.getDatasets();

        for (String datasetName: datasetList){
            if (!datasets.contains(datasetName)){
                throw new IllegalArgumentException();
            }
        }

        for (String datasetName: datasetList) {

            for (String partName: localBackupRepository.getParts(datasetName)){

                Path path = localBackupRepository.getPart(datasetName,partName);
                snapshotReceiver.receiveSnapshotPart(path);
            }

//            String currentPart = null;
//
//            try {
//                while (true) {
//                    try {
//                        Path path = localBackupRepository.getNextPart(datasetName, currentPart);
//                        currentPart = path.getFileName().toString();
//                        snapshotReceiver.receiveSnapshotPart(path);
//                        localBackupRepository.clear(datasetName, currentPart);
//                    } catch (NoMorePartsException e) {
//                        logger.debug("No acceptable files found. Waiting 1 second before retry");
//                        Thread.sleep(1000);
//                    } catch (FinishedFlagException e) {
//                        logger.info("Finish flag found. Exiting loop");
//                        break;
//                    }
//                }
//            } finally {
//                snapshotReceiver.finish();
//            }
        }
    }
}
