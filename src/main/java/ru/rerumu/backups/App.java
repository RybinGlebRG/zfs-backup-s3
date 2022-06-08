package ru.rerumu.backups;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.controllers.BackupController;
import ru.rerumu.backups.controllers.RestoreController;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.models.ZFSPool;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.file.Paths;

@Generated
public class App {

    public static void main(String[] args) {

        try {
            Logger logger = LoggerFactory.getLogger(App.class);
            logger.info("Starting");
            String mode = System.getProperty("mode");
            logger.info("Mode is '"+mode+"'");
            Configuration configuration = new Configuration();

            switch (mode) {
                case "backupFull": {
                    BackupController backupController = new BackupController();
                    backupController.backupFull(
                            configuration.getProperty("full.snapshot"),
                            configuration.getProperty("backup.directory"),
                            new S3Storage[]{
                                    new S3Storage(
                                            Region.of(configuration.getProperty("s3.region_name")),
                                            configuration.getProperty("s3.full.s3_bucket"),
                                            configuration.getProperty("s3.access_key_id"),
                                            configuration.getProperty("s3.secret_access_key"),
                                            Paths.get(configuration.getProperty("s3.full.prefix")),
                                            new URI(configuration.getProperty("s3.endpoint_url")),
                                            configuration.getProperty("s3.full.storage_class")
                                    )
                            },
                            configuration.getProperty("password"),
                            Integer.parseInt(configuration.getProperty("chunk.size")),
                            Boolean.parseBoolean(configuration.getProperty("is.load.aws")),
                            Long.parseLong(configuration.getProperty("file.part.size"))

                    );
                    break;
                }
                case "restore": {
                    RestoreController restoreController = new RestoreController();
                    restoreController.restore(
                            configuration.getProperty("backup.directory"),
                            configuration.getProperty("password"),
                            Boolean.parseBoolean(configuration.getProperty("is.delete.after.receive")),
                            new ZFSPool(configuration.getProperty("receive.pool"))
                    );
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
