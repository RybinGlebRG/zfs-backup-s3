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
import ru.rerumu.backups.services.S3Loader;
import ru.rerumu.backups.services.ZFSBackupsService;
import ru.rerumu.backups.services.ZFSSend;
import ru.rerumu.backups.services.ZFSSendFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.net.URI;
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
        ZFSBackupsService zfsBackupsService = new ZFSBackupsService(password);
        ZFSSendFactory zfsSendFactory = new ZFSSendFactory();
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

        zfsBackupsService.zfsSend(
                zfsSendFull,
                chunkSize,
                isLoadAWS,
                filePartSize,
                filePartRepository,
                isDeleteAfterUpload,
                s3Loader
        );

        // TODO: Kill processes and threads if exception

    }
}
