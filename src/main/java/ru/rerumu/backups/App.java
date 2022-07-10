package ru.rerumu.backups;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.controllers.BackupController;
import ru.rerumu.backups.controllers.RestoreController;
import ru.rerumu.backups.factories.SnapshotSenderFactory;
import ru.rerumu.backups.factories.ZFSFileReaderFactory;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.factories.impl.*;
import ru.rerumu.backups.factories.ZFSFileReaderFactory;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.factories.impl.ZFSFileReaderFactoryImpl;
import ru.rerumu.backups.factories.impl.ZFSFileWriterFactoryImpl;
import ru.rerumu.backups.factories.impl.ZFSProcessFactoryImpl;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.repositories.FilePartRepository;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.repositories.impl.FilePartRepositoryImpl;
import ru.rerumu.backups.repositories.impl.S3Repository;
import ru.rerumu.backups.repositories.impl.ZFSFileSystemRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSSnapshotRepositoryImpl;
import ru.rerumu.backups.services.*;
import ru.rerumu.backups.services.impl.*;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
                    FilePartRepository filePartRepository = new FilePartRepositoryImpl(
                            Paths.get(configuration.getProperty("backup.directory"))
                    );
                    List<S3Storage> s3StorageList = new ArrayList<>();
                    s3StorageList.add(new S3Storage(
                            Region.of(configuration.getProperty("s3.region_name")),
                            configuration.getProperty("s3.full.s3_bucket"),
                            configuration.getProperty("s3.access_key_id"),
                            configuration.getProperty("s3.secret_access_key"),
                            Paths.get(configuration.getProperty("s3.full.prefix")),
                            new URI(configuration.getProperty("s3.endpoint_url")),
                            configuration.getProperty("s3.full.storage_class")
                    ));
                    S3Repository s3Repository = new S3Repository(
                            s3StorageList,
                            new UploadManagerFactoryImpl(
                                    Integer.parseInt(configuration.getProperty("max_part_size"))
                            )
                    );

                    ZFSProcessFactory zfsProcessFactory = new ZFSProcessFactoryImpl(
                            Boolean.parseBoolean(configuration.getProperty("is.multi.incremental")),
                            Boolean.parseBoolean(configuration.getProperty("is.native.encrypted")),
                            new ProcessWrapperFactoryImpl()
                    );
                    ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactory);
                    ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(zfsProcessFactory,zfsSnapshotRepository);
                    ZFSFileWriterFactory zfsFileWriterFactory = new ZFSFileWriterFactoryImpl(
                            configuration.getProperty("password"),
                            Integer.parseInt(configuration.getProperty("chunk.size")),
                            Long.parseLong(configuration.getProperty("file.part.size")));
                    SnapshotSenderFactory snapshotSenderFactory = new SnapshotSenderFactoryImpl(
                            Boolean.parseBoolean(configuration.getProperty("is.multi.incremental")),
                            filePartRepository,
                            s3Repository,
                            zfsProcessFactory,
                            zfsFileWriterFactory,
                            Boolean.parseBoolean(configuration.getProperty("is.load.aws"))
                    );

                    ZFSBackupService zfsBackupService = new ZFSBackupService(
                            zfsFileSystemRepository,
                            snapshotSenderFactory.getSnapshotSender(),
                            new DatasetPropertiesChecker(
                                    Boolean.parseBoolean(configuration.getProperty("is.native.encrypted"))
                            )
                    );

                    BackupController backupController = new BackupController(zfsBackupService);
                    backupController.backupFull(configuration.getProperty("full.snapshot"));
                    break;
                }
                case "restore": {
                    FilePartRepository filePartRepository = new FilePartRepositoryImpl(
                            Paths.get(configuration.getProperty("backup.directory"))
                    );

                    ZFSProcessFactory zfsProcessFactory = new ZFSProcessFactoryImpl(
                            Boolean.parseBoolean(configuration.getProperty("is.multi.incremental")),
                            Boolean.parseBoolean(configuration.getProperty("is.native.encrypted")),
                            new ProcessWrapperFactoryImpl()
                    );
                    ZFSFileReaderFactory zfsFileReaderFactory = new ZFSFileReaderFactoryImpl(configuration.getProperty("password"));
                    SnapshotReceiver snapshotReceiver = new SnapshotReceiverImpl(
                            zfsProcessFactory,
                            new ZFSPool(configuration.getProperty("receive.pool")),
                            filePartRepository,
                            zfsFileReaderFactory,
                            Boolean.parseBoolean(configuration.getProperty("is.delete.after.receive")));

                    ZFSRestoreService zfsRestoreService = new ZFSRestoreService(
                            configuration.getProperty("password"),
                            zfsProcessFactory,
                            Boolean.parseBoolean(configuration.getProperty("is.delete.after.receive")),
                            filePartRepository,
                            snapshotReceiver);

                    RestoreController restoreController = new RestoreController(zfsRestoreService);
                    restoreController.restore();
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
