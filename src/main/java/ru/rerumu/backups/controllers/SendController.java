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
import ru.rerumu.backups.repositories.impl.FilePartRepositoryImpl;
import ru.rerumu.backups.services.*;

import java.io.IOException;
import java.nio.file.Paths;

public class SendController {

    private final Logger logger = LoggerFactory.getLogger(App.class);

    public void sendFull(
            String fullSnapshot,
            String backupDirectory,
            S3Storage[] s3Storages,
            String password,
            int chunkSize,
            boolean isLoadAWS,
            long filePartSize,
            boolean isDeleteAfterUpload) throws IOException, CompressorException, InterruptedException, EncryptException {

        ZFSSendFactory zfsSendFactory = new ZFSSendFactory();
        ZFSBackupsService zfsBackupsService = new ZFSBackupsService(password,new ZFSProcessFactory());
        logger.info("Start 'sendFull'");
        ZFSSend zfsSendFull = zfsSendFactory.getZFSSendFull(fullSnapshot);
        FilePartRepository filePartRepository = new FilePartRepositoryImpl(
                Paths.get(backupDirectory),
                new SnapshotRepository(new Snapshot(fullSnapshot)).getLastFullSnapshot().getName()
        );
        S3Loader s3Loader = new S3Loader();

        for (S3Storage s3Storage : s3Storages){
            s3Loader.addStorage(s3Storage);
        }

        zfsBackupsService.zfsSendFull(
                chunkSize,
                isLoadAWS,
                filePartSize,
                filePartRepository,
                s3Loader,
                fullSnapshot
        );

        // TODO: Kill processes and threads if exception

    }
}
