package ru.rerumu.backups;

import ru.rerumu.backups.services.s3.factories.S3ClientFactory;
import ru.rerumu.backups.services.s3.factories.impl.S3ClientFactoryImpl;
import ru.rerumu.backups.services.s3.models.S3Storage;
import ru.rerumu.backups.services.ReceiveService;
import ru.rerumu.backups.services.SendService;
import ru.rerumu.backups.services.zfs.SnapshotNamingService;
import ru.rerumu.backups.services.zfs.SnapshotService;
import ru.rerumu.backups.services.impl.ReceiveServiceImpl;
import ru.rerumu.backups.services.impl.SendServiceImpl;
import ru.rerumu.backups.services.zfs.factories.StdConsumerFactory;
import ru.rerumu.backups.services.zfs.factories.ZFSFileReaderFactory;
import ru.rerumu.backups.services.zfs.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.services.zfs.factories.impl.StdConsumerFactoryImpl;
import ru.rerumu.backups.services.zfs.factories.impl.ZFSFileReaderFactoryImpl;
import ru.rerumu.backups.services.zfs.factories.impl.ZFSFileWriterFactoryImpl;
import ru.rerumu.backups.services.zfs.impl.SnapshotNamingServiceImpl;
import ru.rerumu.backups.services.zfs.impl.SnapshotServiceImpl;
import ru.rerumu.backups.services.s3.FileManager;
import ru.rerumu.backups.services.s3.S3Service;
import ru.rerumu.backups.services.s3.factories.S3CallableFactory;
import ru.rerumu.backups.services.s3.factories.impl.S3CallableFactoryImpl;
import ru.rerumu.backups.services.s3.impl.FileManagerImpl;
import ru.rerumu.backups.services.s3.impl.S3ServiceImpl;
import ru.rerumu.backups.services.s3.repositories.impl.S3RepositoryImpl;
import ru.rerumu.backups.services.s3.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.services.zfs.ZFSService;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;
import ru.rerumu.backups.services.zfs.factories.impl.ZFSCallableFactoryImpl;
import ru.rerumu.backups.services.zfs.impl.ZFSServiceImpl;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.impl.ProcessFactoryImpl;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntityFactory {
    private final Configuration configuration = new Configuration();

    public EntityFactory() throws IOException {
    }

    public SendService getSendService() throws URISyntaxException {
        S3Storage s3Storage = new S3Storage(
                Region.of(configuration.getProperty("s3.region_name")),
                configuration.getProperty("s3.full.s3_bucket"),
                configuration.getProperty("s3.access_key_id"),
                configuration.getProperty("s3.secret_access_key"),
                Paths.get(configuration.getProperty("s3.full.prefix")),
                new URI(configuration.getProperty("s3.endpoint_url")),
                configuration.getProperty("s3.full.storage_class")
        );
        S3ClientFactory s3ClientFactory = new S3ClientFactoryImpl(
                List.of(s3Storage)
        );
        S3CallableFactory s3CallableFactory = new S3CallableFactoryImpl(
                Integer.parseInt(configuration.getProperty("max_part_size")),
                s3Storage,
                s3ClientFactory
        );
        S3Service s3Service = new S3ServiceImpl(s3CallableFactory);
        ru.rerumu.backups.services.s3.repositories.S3Repository s3Repository = new S3RepositoryImpl(
                s3Storage,
                s3Service
        );
        ZFSFileWriterFactory zfsFileWriterFactory = new ZFSFileWriterFactoryImpl(
                Long.parseLong(configuration.getProperty("max_file_size"))
        );
        ZFSFileReaderFactory zfsFileReaderFactory = new ZFSFileReaderFactoryImpl();
        FileManager fileManager = new FileManagerImpl(
                UUID.randomUUID().toString(),
                Paths.get(configuration.getProperty("sender_temp_dir"))
        );
        S3StreamRepositoryImpl s3StreamRepository = new S3StreamRepositoryImpl(
                s3Repository,
                zfsFileWriterFactory,
                zfsFileReaderFactory,
                fileManager
        );
        ExecutorService executorService = Executors.newCachedThreadPool();
        ProcessFactory processFactory = new ProcessFactoryImpl(executorService);

        SnapshotNamingService snapshotNamingService = new SnapshotNamingServiceImpl();
        ZFSService zfsService = null;
        StdConsumerFactory stdConsumerFactory = new StdConsumerFactoryImpl();
        ZFSCallableFactory zfsCallableFactory = new ZFSCallableFactoryImpl(processFactory,executorService, zfsService,stdConsumerFactory);
        SnapshotService snapshotService = new SnapshotServiceImpl(zfsCallableFactory);
        zfsService = new ZFSServiceImpl(zfsCallableFactory);

        SendService sendService = new SendServiceImpl(
                s3StreamRepository,
                snapshotService,
                snapshotNamingService,
                zfsService,
                zfsCallableFactory,
                stdConsumerFactory
        );
        return sendService;
    }

    public ReceiveService getReceiveService() throws URISyntaxException {
        S3Storage s3Storage = new S3Storage(
                Region.of(configuration.getProperty("s3.region_name")),
                configuration.getProperty("s3.full.s3_bucket"),
                configuration.getProperty("s3.access_key_id"),
                configuration.getProperty("s3.secret_access_key"),
                Paths.get(configuration.getProperty("s3.full.prefix")),
                new URI(configuration.getProperty("s3.endpoint_url")),
                configuration.getProperty("s3.full.storage_class")
        );
        S3ClientFactory s3ClientFactory = new S3ClientFactoryImpl(
                List.of(s3Storage)
        );
        S3CallableFactory s3CallableFactory = new S3CallableFactoryImpl(
                Integer.parseInt(configuration.getProperty("max_part_size")),
                s3Storage,
                s3ClientFactory
        );
        S3Service s3Service = new S3ServiceImpl(s3CallableFactory);
        ru.rerumu.backups.services.s3.repositories.S3Repository s3Repository = new S3RepositoryImpl(
                s3Storage,
                s3Service
        );
        ZFSFileWriterFactory zfsFileWriterFactory = new ZFSFileWriterFactoryImpl(
                Long.parseLong(configuration.getProperty("max_file_size"))
        );
        ZFSFileReaderFactory zfsFileReaderFactory = new ZFSFileReaderFactoryImpl();
        FileManager fileManager = new FileManagerImpl(
                UUID.randomUUID().toString(),
                Paths.get(configuration.getProperty("sender_temp_dir"))
        );
        S3StreamRepositoryImpl s3StreamRepository = new S3StreamRepositoryImpl(
                s3Repository,
                zfsFileWriterFactory,
                zfsFileReaderFactory,
                fileManager
        );
        ExecutorService executorService = Executors.newCachedThreadPool();
        ProcessFactory processFactory = new ProcessFactoryImpl(executorService);
        ZFSService zfsService = null;
        StdConsumerFactory stdConsumerFactory = new StdConsumerFactoryImpl();
        ZFSCallableFactory zfsCallableFactory = new ZFSCallableFactoryImpl(processFactory,executorService,zfsService,stdConsumerFactory);
        zfsService = new ZFSServiceImpl(zfsCallableFactory);
        SnapshotNamingService snapshotNamingService = new SnapshotNamingServiceImpl();
        ReceiveService receiveService = new ReceiveServiceImpl(
                s3StreamRepository,
                zfsService,
                snapshotNamingService,
                zfsCallableFactory
        );
        return receiveService;
    }
}
