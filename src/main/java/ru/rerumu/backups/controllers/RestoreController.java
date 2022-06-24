package ru.rerumu.backups.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.ZFSRestoreService;

public class RestoreController {
    private final Logger logger = LoggerFactory.getLogger(RestoreController.class);

    private final ZFSRestoreService zfsRestoreService;

    public RestoreController(ZFSRestoreService zfsRestoreService){
        this.zfsRestoreService = zfsRestoreService;
    }

    public void restore(){
        try {
            zfsRestoreService.zfsReceive();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
