package ru.rerumu.backups.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.services.*;

public class BackupController {

    private final Logger logger = LoggerFactory.getLogger(BackupController.class);

    private final ZFSBackupService zfsBackupService;

    public BackupController(
            ZFSBackupService zfsBackupService
    ){
        this.zfsBackupService = zfsBackupService;
    }

    public int backupFull(
            String fullSnapshot){
        try {
            logger.info("Start 'sendFull'");

            Snapshot targetSnapshot = new Snapshot(fullSnapshot);
            zfsBackupService.zfsBackupFull(
                    targetSnapshot.getName(),
                    targetSnapshot.getDataset()
            );
            return 0;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 1;
        }
    }

    public int backupIncremental(
            String baseSnapshotName,
            String targetSnapshotName
    ){
        try {
            logger.info("Starting incremental backup");

            Snapshot baseSnapshot = new Snapshot(baseSnapshotName);
            Snapshot targetSnapshot = new Snapshot(targetSnapshotName);
            zfsBackupService.zfsBackupIncremental(
                    baseSnapshot.getDataset(),
                    baseSnapshot.getName(),
                    targetSnapshot.getName()
            );
            return 0;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 1;
        }
    }
}
