package ru.rerumu.backups.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.Generated;
import ru.rerumu.backups.services.ZFSFileReaderFactory;
import ru.rerumu.backups.services.impl.ZFSFileReaderFactoryImpl;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.impl.FilePartRepositoryImpl;
import ru.rerumu.backups.services.SnapshotReceiver;
import ru.rerumu.backups.services.ZFSProcessFactory;
import ru.rerumu.backups.services.impl.SnapshotReceiverImpl;
import ru.rerumu.backups.services.impl.ZFSProcessFactoryImpl;
import ru.rerumu.backups.services.ZFSRestoreService;

import java.nio.file.Paths;

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
