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
            EntityFactory entityFactory = new EntityFactory();

            switch (mode) {
                case "backupFull": {
                    FilePartRepository filePartRepository = entityFactory.getFilePartRepository();
                    List<S3Storage> s3StorageList = entityFactory.getS3StorageList();
                    S3Repository s3Repository = entityFactory.getS3Repository(s3StorageList);
                    ZFSProcessFactory zfsProcessFactory = entityFactory.getZFSProcessFactory();
                    ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactory);
                    ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(zfsProcessFactory,zfsSnapshotRepository);
                    ZFSFileWriterFactory zfsFileWriterFactory = entityFactory.getZFSFileWriterFactory();
                    SnapshotSenderFactory snapshotSenderFactory = entityFactory.getSnapshotSenderFactory(
                            filePartRepository,
                            s3Repository,
                            zfsProcessFactory,
                            zfsFileWriterFactory
                    );

                    ZFSBackupService zfsBackupService = new ZFSBackupService(
                            zfsFileSystemRepository,
                            snapshotSenderFactory.getSnapshotSender(),
                            new DatasetPropertiesChecker()
                    );

                    BackupController backupController = new BackupController(zfsBackupService);
                    backupController.backupFull(configuration.getProperty("full.snapshot"));
                    break;
                }
                case "backupInc":{
                    FilePartRepository filePartRepository = entityFactory.getFilePartRepository();
                    List<S3Storage> s3StorageList = entityFactory.getS3StorageList();
                    S3Repository s3Repository = entityFactory.getS3Repository(s3StorageList);
                    ZFSProcessFactory zfsProcessFactory = entityFactory.getZFSProcessFactory();
                    ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactory);
                    ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(zfsProcessFactory,zfsSnapshotRepository);
                    ZFSFileWriterFactory zfsFileWriterFactory = entityFactory.getZFSFileWriterFactory();
                    SnapshotSenderFactory snapshotSenderFactory = entityFactory.getSnapshotSenderFactory(
                            filePartRepository,
                            s3Repository,
                            zfsProcessFactory,
                            zfsFileWriterFactory
                    );

                    ZFSBackupService zfsBackupService = new ZFSBackupService(
                            zfsFileSystemRepository,
                            snapshotSenderFactory.getSnapshotSender(),
                            new DatasetPropertiesChecker()
                    );

                    BackupController backupController = new BackupController(zfsBackupService);
                    backupController.backupIncremental(
                            configuration.getProperty("full.snapshot"),
                            configuration.getProperty("incremental_snapshot")
                    );
                    break;
                }
                case "restore": {
                    FilePartRepository filePartRepository = entityFactory.getFilePartRepository();

                    ZFSProcessFactory zfsProcessFactory = entityFactory.getZFSProcessFactory();
                    ZFSFileReaderFactory zfsFileReaderFactory = new ZFSFileReaderFactoryImpl();
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
