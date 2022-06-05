package ru.rerumu.backups.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.io.S3Loader;
import ru.rerumu.backups.io.ZFSFileWriterFactory;
import ru.rerumu.backups.io.impl.ZFSFileWriterFactoryImpl;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.repositories.impl.FilePartRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSFileSystemRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSSnapshotRepositoryImpl;
import ru.rerumu.backups.services.*;
import ru.rerumu.backups.io.impl.S3LoaderImpl;
import ru.rerumu.backups.services.impl.SnapshotSenderImpl;
import ru.rerumu.backups.services.impl.ZFSProcessFactoryImpl;

import java.nio.file.Paths;

public class BackupController {

    private final Logger logger = LoggerFactory.getLogger(BackupController.class);

    public void backupFull(
            String fullSnapshot,
            String backupDirectory,
            S3Storage[] s3Storages,
            String password,
            int chunkSize,
            boolean isLoadAWS,
            long filePartSize){
        try {
            FilePartRepository filePartRepository = new FilePartRepositoryImpl(
                    Paths.get(backupDirectory)
            );

            S3Loader s3Loader = new S3LoaderImpl();
            for (S3Storage s3Storage : s3Storages) {
                s3Loader.addStorage(s3Storage);
            }

            ZFSProcessFactory zfsProcessFactory = new ZFSProcessFactoryImpl();
            ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactory);
            ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(zfsProcessFactory,zfsSnapshotRepository);
            ZFSFileWriterFactory zfsFileWriterFactory = new ZFSFileWriterFactoryImpl(password, chunkSize, filePartSize);
            SnapshotSender snapshotSender = new SnapshotSenderImpl(filePartRepository, s3Loader,zfsProcessFactory,zfsFileWriterFactory,isLoadAWS);

            ZFSBackupService zfsBackupService = new ZFSBackupService(
                    isLoadAWS,
                    zfsFileSystemRepository,
                    snapshotSender
                    );
            logger.info("Start 'sendFull'");

            Snapshot targetSnapshot = new Snapshot(fullSnapshot);
            zfsBackupService.zfsBackupFull(
                    s3Loader,
                    targetSnapshot.getName(),
                    targetSnapshot.getDataset()
            );

            // TODO: Kill processes and threads if exception
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
