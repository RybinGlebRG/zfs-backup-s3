package ru.rerumu.backups.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.App;
import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.SnapshotRepository;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.repositories.impl.FilePartRepositoryImpl;
import ru.rerumu.backups.services.*;

import java.io.IOException;
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
            long filePartSize,
            boolean isDeleteAfterUpload){
        try {
            FilePartRepository filePartRepository = new FilePartRepositoryImpl(
                    Paths.get(backupDirectory)
            );

            ZFSBackupService zfsBackupService = new ZFSBackupService(
                    password,
                    new ZFSProcessFactory(),
                    chunkSize,
                    isLoadAWS,
                    filePartSize,
                    filePartRepository);
            logger.info("Start 'sendFull'");



            ZFSFileSystemRepository zfsFileSystemRepository;
            ZFSSnapshotRepository zfsSnapshotRepository;
            S3Loader s3Loader = new S3Loader();

            for (S3Storage s3Storage : s3Storages) {
                s3Loader.addStorage(s3Storage);
            }

            zfsBackupService.zfsBackupFull(
                    filePartRepository,
                    s3Loader,
                    zfsFileSystemRepository,
                    zfsSnapshotRepository,
                    fullSnapshot
            );

            // TODO: Kill processes and threads if exception
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
