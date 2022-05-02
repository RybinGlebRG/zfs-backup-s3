package ru.rerumu.backups.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.impl.FilePartRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSFileSystemRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSSnapshotRepositoryImpl;
import ru.rerumu.backups.services.*;
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

            ZFSBackupService zfsBackupService = new ZFSBackupService(
                    password,
                    new ZFSProcessFactoryImpl(),
                    chunkSize,
                    isLoadAWS,
                    filePartSize,
                    filePartRepository,
                    new ZFSFileSystemRepositoryImpl(new ZFSProcessFactoryImpl()),
                    new ZFSSnapshotRepositoryImpl(new ZFSProcessFactoryImpl()));
            logger.info("Start 'sendFull'");

            S3Loader s3Loader = new S3Loader();

            for (S3Storage s3Storage : s3Storages) {
                s3Loader.addStorage(s3Storage);
            }

            zfsBackupService.zfsBackupFull(
                    s3Loader,
                    new Snapshot(fullSnapshot)
            );

            // TODO: Kill processes and threads if exception
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
