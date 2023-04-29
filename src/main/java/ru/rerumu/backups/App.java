package ru.rerumu.backups;


import org.apache.commons.cli.*;
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

            Options options = new Options();
            options.addOption("p", "pool", true, "pool to backup");
            options.addOption("b", "bucket", true, "S3 Bucket in which to store backup");
            options.addOption("h", "help", true, "print this message");
            options.addOption("m","mode", true,"'full' for full backup");
            options.addOption("s","snapshot", true,"snapshot to restore");

            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            HelpFormatter formatter = new HelpFormatter();

            if (cmd.hasOption("h")) {
                formatter.printHelp("zfs-s3-backup", options);
                return;
            }

            String poolName;
            String bucketName;
            String mode = "backupFull";
            String snapshotName = null;

            if (cmd.hasOption("p") && cmd.hasOption("b")) {
                poolName = cmd.getOptionValue("p");
                bucketName = cmd.getOptionValue("b");
            } else {
                throw new IllegalArgumentException("Pool and/or s3 bucket are not specified");
            }

            if(cmd.hasOption("m") ){
                mode = switch (cmd.getOptionValue("m")){
                    case "full" ->"backupFull";
                    case "restore" -> "restore";
                    default -> throw new IllegalArgumentException("Incorrect mode value");
                };
            }

            if (mode.equals("restore")){
                if (cmd.hasOption("s")) {
                    snapshotName = cmd.getOptionValue("s");
                } else {
                    throw new IllegalArgumentException("Snapshot is not specified");
                }
            }

            logger.info("Starting");
            logger.info("Mode is '" + mode + "'");
//            Configuration configuration = new Configuration();
            EntityFactory entityFactory = new EntityFactory();

            switch (mode) {
                case "backupFull" -> {

                    SendService sendService = entityFactory.getSendService();

                    BackupController backupController = new BackupController(sendService);
                    backupController.backupFull(poolName,bucketName);

                }
//                case "backupInc" -> {
//                    List<S3Storage> s3StorageList = entityFactory.getS3StorageList();
//                    S3Repository s3Repository = entityFactory.getS3Repository(s3StorageList);
//                    LocalBackupRepository localBackupRepository = entityFactory.getLocalBackupRepository(s3Repository);
//                    ZFSProcessFactory zfsProcessFactory = entityFactory.getZFSProcessFactory();
//                    ZFSSnapshotRepository zfsSnapshotRepository = new ZFSSnapshotRepositoryImpl(zfsProcessFactory);
//                    ZFSFileSystemRepository zfsFileSystemRepository = new ZFSFileSystemRepositoryImpl(zfsProcessFactory, zfsSnapshotRepository);
//                    ZFSFileWriterFactory zfsFileWriterFactory = entityFactory.getZFSFileWriterFactory();
//                    SnapshotSenderFactory snapshotSenderFactory = entityFactory.getSnapshotSenderFactory(
//                            localBackupRepository,
//                            zfsProcessFactory,
//                            zfsFileWriterFactory
//                    );
//
//                    ZFSBackupService zfsBackupService = new ZFSBackupService(
//                            zfsFileSystemRepository,
//                            snapshotSenderFactory.getSnapshotSender(),
//                            new DatasetPropertiesChecker()
//                    );
//
//                    BackupController backupController = new BackupController(zfsBackupService);
//                    backupController.backupIncremental(
//                            configuration.getProperty("full.snapshot"),
//                            configuration.getProperty("incremental_snapshot")
//                    );
//                }
                case "restore" -> {

                    ReceiveService receiveService = entityFactory.getReceiveService();

                    RestoreController restoreController = new RestoreController(receiveService);
                    restoreController.restore(bucketName,poolName);

//                    List<S3Storage> s3StorageList = entityFactory.getS3StorageList();
//                    RemoteBackupRepository s3Repository = entityFactory.getS3Repository(s3StorageList);
//                    LocalBackupRepository localBackupRepository = entityFactory.getLocalBackupRepository(s3Repository);
//
//                    ZFSProcessFactory zfsProcessFactory = entityFactory.getZFSProcessFactory();
//                    ZFSFileReaderFactory zfsFileReaderFactory = new ZFSFileReaderFactoryImpl();
//                    SnapshotReceiver snapshotReceiver = new SnapshotReceiverImpl(
//                            zfsProcessFactory,
//                            new ZFSPool(configuration.getProperty("receive.pool")),
//                            zfsFileReaderFactory
//                    );
//
//                    ZFSRestoreService zfsRestoreService = new ZFSRestoreService(
//                            localBackupRepository,
//                            snapshotReceiver,
//                            Arrays.asList(
//                                    configuration.getProperty("restore.dataset").split(",")
//                            )
//                    );
//
//                    RestoreController restoreController = new RestoreController(zfsRestoreService);
//                    restoreController.restore();
                }
                default -> throw new IllegalArgumentException();
            }
            logger.info("Finished");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
