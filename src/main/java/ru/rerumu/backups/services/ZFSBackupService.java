package ru.rerumu.backups.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.services.impl.SnapshotPickerImpl;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ZFSBackupService {

    private final Logger logger = LoggerFactory.getLogger(ZFSBackupService.class);
    private final SnapshotPicker snapshotPicker = new SnapshotPickerImpl();
    private final ZFSFileSystemRepository zfsFileSystemRepository;
    private final SnapshotSender snapshotSender;
    private final DatasetPropertiesChecker datasetPropertiesChecker;

    public ZFSBackupService(ZFSFileSystemRepository zfsFileSystemRepository,
                            SnapshotSender snapshotSender,
                            DatasetPropertiesChecker datasetPropertiesChecker) {
        this.zfsFileSystemRepository = zfsFileSystemRepository;
        this.snapshotSender = snapshotSender;
        this.datasetPropertiesChecker = datasetPropertiesChecker;
    }

    public void zfsBackupFull(String targetSnapshotName,
                              String parentDatasetName) throws
            IOException,
            InterruptedException,
            CompressorException,
            EncryptException,
            BaseSnapshotNotFoundException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            ExecutionException,
            S3MissesFileException,
            IncompatibleDatasetException {

        List<ZFSDataset> zfsDatasetList = zfsFileSystemRepository.getFilesystemsTreeList(parentDatasetName);

        for (ZFSDataset zfsDataset : zfsDatasetList) {
            try {
                datasetPropertiesChecker.check(zfsDataset);
                List<Snapshot> pickedSnapshots = snapshotPicker.pick(zfsDataset, targetSnapshotName);
                snapshotSender.sendStartingFromFull(zfsDataset.getName(),pickedSnapshots);
            } catch (SnapshotNotFoundException e){
                logger.warn(String.format("Skipping filesystem '%s'. No acceptable snapshots", zfsDataset.getName()));
            }

        }
        logger.debug("Sent all filesystems");
    }

//    public void zfsBackupIncremental(S3LoaderImpl s3Loader,
//                                     String baseSnapshotName,
//                                     String targetSnapshotName,
//                                     String parentDatasetName) throws
//            IOException,
//            InterruptedException,
//            CompressorException,
//            EncryptException, SnapshotNotFoundException {
//
//        List<ZFSFileSystem> zfsFileSystemList = zfsFileSystemRepository.getFilesystemsTreeList(parentDatasetName);
//
//        for (ZFSFileSystem zfsFileSystem : zfsFileSystemList) {
//            List<Snapshot> incrementalSnapshots = zfsFileSystem.getIncrementalSnapshots(baseSnapshotName, targetSnapshotName);
//            Snapshot baseSnapshot = incrementalSnapshots.remove(0);
//
//            sendIncrementalSnapshots(baseSnapshot, incrementalSnapshots, s3Loader);
//        }
//    }
}
