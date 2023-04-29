package ru.rerumu.backups.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.ReceiveService;

public class RestoreController {
    private final Logger logger = LoggerFactory.getLogger(RestoreController.class);
    private final ReceiveService receiveService;

    public RestoreController(ReceiveService receiveService) {
        this.receiveService = receiveService;
    }


    public void restore(String bucketName, String poolName){
        try {
            receiveService.receive(bucketName, poolName);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
