package ru.rerumu.backups;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.controllers.SendController;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.impl.FilePartRepositoryImpl;
import ru.rerumu.backups.repositories.SnapshotRepository;
import ru.rerumu.backups.services.*;
import ru.rerumu.backups.services.S3Loader;
import ru.rerumu.backups.services.impl.AESCryptor;
import ru.rerumu.backups.services.impl.ZFSReceiveImpl;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.file.Paths;

public class App {

    public static void main(String[] args) {

        try {
            Logger logger = LoggerFactory.getLogger(App.class);
            logger.info("Starting");
            String mode = System.getProperty("mode");
            logger.info("Mode is '"+mode+"'");
            Configuration configuration = new Configuration();
            ZFSBackupsService zfsBackupsService = new ZFSBackupsService(
                    configuration.getProperty("password")
            );
            switch (mode) {
                case "sendFull": {
                    SendController sendController = new SendController();
                    sendController.sendFull(
                            configuration.getProperty("full.snapshot"),
                            configuration.getProperty("backup.directory"),
                            new S3Storage[]{
//                                    new S3Storage(
//                                            Region.of(configuration.getProperty("s3.aws.region_name")),
//                                            configuration.getProperty("s3.aws.full.s3_bucket"),
//                                            configuration.getProperty("s3.aws.access_key_id"),
//                                            configuration.getProperty("s3.aws.secret_access_key"),
//                                            Paths.get(configuration.getProperty("s3.aws.full.prefix")),
//                                            new URI(configuration.getProperty("s3.aws.endpoint_url")),
//                                            configuration.getProperty("s3.aws.full.storage_class")
//                                    ),
                                    new S3Storage(
                                            Region.of(configuration.getProperty("s3.yandex.region_name")),
                                            configuration.getProperty("s3.yandex.full.s3_bucket"),
                                            configuration.getProperty("s3.yandex.access_key_id"),
                                            configuration.getProperty("s3.yandex.secret_access_key"),
                                            Paths.get(configuration.getProperty("s3.yandex.full.prefix")),
                                            new URI(configuration.getProperty("s3.yandex.endpoint_url")),
                                            configuration.getProperty("s3.yandex.full.storage_class")
                                    )
                            },
                            configuration.getProperty("password"),
                            Integer.parseInt(configuration.getProperty("chunk.size")),
                            Boolean.parseBoolean(configuration.getProperty("is.load.aws")),
                            Long.parseLong(configuration.getProperty("file.part.size")),
                            Boolean.parseBoolean(configuration.getProperty("is.delete.after.upload"))

                    );
                    break;
                }
                case "receive": {
                    ZFSReceiveImpl zfsReceive = new ZFSReceiveImpl(configuration.getProperty("receive.pool"));
                    FilePartRepository filePartRepository = new FilePartRepositoryImpl(
                            Paths.get(configuration.getProperty("backup.directory")),
                            new SnapshotRepository(new Snapshot(configuration.getProperty("full.snapshot"))).getLastFullSnapshot().getName()
                    );

                    zfsBackupsService.zfsReceive(
                            zfsReceive,
                            filePartRepository,
                            Boolean.parseBoolean(configuration.getProperty("is.delete.after.receive")));
                    break;
                }
                default:
                    throw new IllegalArgumentException();
            }
            logger.info("Finished");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
