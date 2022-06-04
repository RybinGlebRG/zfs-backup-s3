package ru.rerumu.backups.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.io.S3Loader;
import ru.rerumu.backups.io.ZFSFileWriter;
import ru.rerumu.backups.io.ZFSFileWriterFactory;
import ru.rerumu.backups.io.impl.ZFSFileWriterFull;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.io.impl.S3LoaderImpl;
import ru.rerumu.backups.services.impl.SnapshotSenderImpl;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class ZFSBackupService {

    private final Logger logger = LoggerFactory.getLogger(ZFSBackupService.class);
    private final boolean isLoadS3;
    private final ZFSFileSystemRepository zfsFileSystemRepository;
    private final SnapshotSender snapshotSender;

    public ZFSBackupService(boolean isLoadS3,
                            ZFSFileSystemRepository zfsFileSystemRepository,
                            SnapshotSender snapshotSender) {
        this.isLoadS3 = isLoadS3;
        this.zfsFileSystemRepository = zfsFileSystemRepository;
        this.snapshotSender = snapshotSender;
    }

    private void sendIncrementalSnapshots(Snapshot baseSnapshot, List<Snapshot> incrementalSnapshots, S3Loader s3Loader)
            throws IOException, CompressorException, InterruptedException, EncryptException, NoSuchAlgorithmException, IncorrectHashException {
        for (Snapshot incrementalSnapshot : incrementalSnapshots) {
            logger.debug(String.format(
                    "Sending incremental snapshot '%s'. Base snapshot - '%s'",
                    incrementalSnapshot.getFullName(),
                    baseSnapshot.getFullName()));

            snapshotSender.sendIncrementalSnapshot(baseSnapshot, incrementalSnapshot, s3Loader, isLoadS3);

            baseSnapshot = incrementalSnapshot;
        }
    }

    public void zfsBackupFull(S3Loader s3Loader,
                              String targetSnapshotName,
                              String parentDatasetName) throws
            IOException,
            InterruptedException,
            CompressorException,
            EncryptException,
            BaseSnapshotNotFoundException, NoSuchAlgorithmException, IncorrectHashException {

        List<ZFSFileSystem> zfsFileSystemList = zfsFileSystemRepository.getFilesystemsTreeList(parentDatasetName);

        for (ZFSFileSystem zfsFileSystem : zfsFileSystemList) {
            if (!zfsFileSystem.isSnapshotExists(targetSnapshotName)) {
                logger.warn(String.format("Skipping filesystem '%s'. No acceptable snapshots", zfsFileSystem.getName()));
                continue;
            }

            List<Snapshot> sentSnapshots = new ArrayList<>();
            Snapshot baseSnapshot = zfsFileSystem.getBaseSnapshot();

            logger.debug(String.format("Sending base snapshot '%s'", baseSnapshot.getFullName()));

            snapshotSender.sendBaseSnapshot(baseSnapshot, s3Loader, isLoadS3);
            sentSnapshots.add(baseSnapshot);

            try {
                List<Snapshot> incrementalSnapshots;
                incrementalSnapshots = zfsFileSystem.getIncrementalSnapshots(targetSnapshotName);
                sendIncrementalSnapshots(baseSnapshot, incrementalSnapshots, s3Loader);
                sentSnapshots.addAll(incrementalSnapshots);
            } catch (SnapshotNotFoundException e) {
                logger.warn(String.format("No acceptable incremental snapshots for filesystem '%s'", zfsFileSystem.getName()));
            }
            snapshotSender.checkSent(sentSnapshots, s3Loader);

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
