package ru.rerumu.backups;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.controllers.BackupController;
import ru.rerumu.backups.controllers.RestoreController;
import ru.rerumu.backups.factories.SnapshotSenderFactory;
import ru.rerumu.backups.factories.ZFSFileReaderFactory;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.factories.impl.ZFSFileReaderFactoryImpl;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.repositories.LocalBackupRepository;
import ru.rerumu.backups.repositories.RemoteBackupRepository;
import ru.rerumu.backups.repositories.ZFSFileSystemRepository;
import ru.rerumu.backups.repositories.ZFSSnapshotRepository;
import ru.rerumu.backups.repositories.impl.S3Repository;
import ru.rerumu.backups.repositories.impl.ZFSFileSystemRepositoryImpl;
import ru.rerumu.backups.repositories.impl.ZFSSnapshotRepositoryImpl;
import ru.rerumu.backups.services.*;
import ru.rerumu.backups.services.impl.*;

import java.util.Arrays;
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

                    List<S3Storage> s3StorageList = entityFactory.getS3StorageList();
                    S3Repository s3Repository = entityFactory.getS3Repository(s3StorageList);
                    LocalBackupRepository localBackupRepository = entityFactory.getLocalBackupRepository(s3Repository);
                    ZFSProcessFactory zfsProcessFactory = entityFactory.getZFSProcessFactory();
                    ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactory);
                    ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(zfsProcessFactory,zfsSnapshotRepository);
                    ZFSFileWriterFactory zfsFileWriterFactory = entityFactory.getZFSFileWriterFactory();
                    SnapshotSenderFactory snapshotSenderFactory = entityFactory.getSnapshotSenderFactory(
                            localBackupRepository,
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
                    List<S3Storage> s3StorageList = entityFactory.getS3StorageList();
                    S3Repository s3Repository = entityFactory.getS3Repository(s3StorageList);
                    LocalBackupRepository localBackupRepository = entityFactory.getLocalBackupRepository(s3Repository);
                    ZFSProcessFactory zfsProcessFactory = entityFactory.getZFSProcessFactory();
                    ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactory);
                    ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(zfsProcessFactory,zfsSnapshotRepository);
                    ZFSFileWriterFactory zfsFileWriterFactory = entityFactory.getZFSFileWriterFactory();
                    SnapshotSenderFactory snapshotSenderFactory = entityFactory.getSnapshotSenderFactory(
                            localBackupRepository,
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
                    List<S3Storage> s3StorageList = entityFactory.getS3StorageList();
                    RemoteBackupRepository s3Repository = entityFactory.getS3Repository(s3StorageList);
                    LocalBackupRepository localBackupRepository = entityFactory.getLocalBackupRepository(s3Repository);

                    ZFSProcessFactory zfsProcessFactory = entityFactory.getZFSProcessFactory();
                    ZFSFileReaderFactory zfsFileReaderFactory = new ZFSFileReaderFactoryImpl();
                    SnapshotReceiver snapshotReceiver = new SnapshotReceiverImpl(
                            zfsProcessFactory,
                            new ZFSPool(configuration.getProperty("receive.pool")),
                            zfsFileReaderFactory
                    );

                    ZFSRestoreService zfsRestoreService = new ZFSRestoreService(
                            localBackupRepository,
                            snapshotReceiver,
                            Arrays.asList(
                                    configuration.getProperty("restore.dataset").split(",")
                            )
                    );

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
