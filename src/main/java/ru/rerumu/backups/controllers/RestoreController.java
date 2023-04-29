package ru.rerumu.backups.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.ReceiveService;
import ru.rerumu.backups.services.ZFSRestoreService;

public class RestoreController {
    private final Logger logger = LoggerFactory.getLogger(RestoreController.class);

    private  ZFSRestoreService zfsRestoreService;
    private  ReceiveService receiveService;

    public RestoreController(ZFSRestoreService zfsRestoreService){
        this.zfsRestoreService = zfsRestoreService;
    }

    public RestoreController(ReceiveService receiveService) {
        this.receiveService = receiveService;
    }

    public int restore(){
        try {
            zfsRestoreService.zfsReceive();
            return 0;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return 1;
        }
    }

    public void restore(String bucketName, String poolName){
        try {
            receiveService.receive(bucketName, poolName);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
