package ru.rerumu.backups.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.s3.Bucket;
import ru.rerumu.backups.models.zfs.Pool;
import ru.rerumu.backups.services.*;

public class BackupController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ZFSBackupService zfsBackupService;
    private SendService sendService;

    public BackupController(
            ZFSBackupService zfsBackupService
    ){
        this.zfsBackupService = zfsBackupService;
    }

    public BackupController(SendService sendService) {
        this.sendService = sendService;
    }

//    public int backupFull(
//            String fullSnapshot){
//        try {
//            logger.info("Start 'sendFull'");
//
//            Snapshot targetSnapshot = new Snapshot(fullSnapshot);
//            zfsBackupService.zfsBackupFull(
//                    targetSnapshot.getName(),
//                    targetSnapshot.getDataset()
//            );
//            return 0;
//
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return 1;
//        }
//    }

    public void backupFull(String poolName, String bucketName){
        try {
            logger.info("Start 'sendFull'");

            sendService.send(poolName,bucketName);

            logger.info("Finished 'sendFull'");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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
