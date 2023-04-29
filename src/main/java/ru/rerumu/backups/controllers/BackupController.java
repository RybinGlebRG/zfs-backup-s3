package ru.rerumu.backups.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.s3.Bucket;
import ru.rerumu.backups.models.zfs.Pool;
import ru.rerumu.backups.services.*;

public class BackupController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SendService sendService;

    public BackupController(SendService sendService) {
        this.sendService = sendService;
    }

    public void backupFull(String poolName, String bucketName){
        try {
            logger.info("Start 'sendFull'");

            sendService.send(poolName,bucketName);

            logger.info("Finished 'sendFull'");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
