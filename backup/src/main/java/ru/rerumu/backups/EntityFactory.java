package ru.rerumu.backups;

import ru.rerumu.backups.factories.StdConsumerFactory;
import ru.rerumu.backups.factories.impl.StdConsumerFactoryImpl;
import ru.rerumu.backups.services.ReceiveService;
import ru.rerumu.backups.services.SendService;
import ru.rerumu.backups.services.impl.ReceiveServiceImpl;
import ru.rerumu.backups.services.impl.SendServiceImpl;
import ru.rerumu.s3.S3ServiceFactory;
import ru.rerumu.s3.S3ServiceFactoryImpl;
import ru.rerumu.s3.S3Service;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.backups.services.SnapshotNamingService;
import ru.rerumu.zfs.ZFSService;
import ru.rerumu.zfs.ZFSServiceFactory;
import ru.rerumu.backups.services.impl.SnapshotNamingServiceImpl;
import ru.rerumu.zfs.ZFSServiceFactoryImpl;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

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
        S3ServiceFactory s3ServiceFactory =  new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                Integer.parseInt(configuration.getProperty("max_part_size")),
                Long.parseLong(configuration.getProperty("max_file_size")),
                Paths.get(configuration.getProperty("sender_temp_dir"))
        );
        SnapshotNamingService snapshotNamingService = new SnapshotNamingServiceImpl();
        ZFSServiceFactory zfsServiceFactory = new ZFSServiceFactoryImpl();
        ZFSService zfsService = zfsServiceFactory.getZFSService();
        StdConsumerFactory stdConsumerFactory = new StdConsumerFactoryImpl(s3Service);
        SendService sendService = new SendServiceImpl(
                snapshotNamingService,
                zfsService,
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
        S3ServiceFactory s3ServiceFactory =  new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                Integer.parseInt(configuration.getProperty("max_part_size")),
                Long.parseLong(configuration.getProperty("max_file_size")),
                Paths.get(configuration.getProperty("sender_temp_dir"))
        );
        ZFSServiceFactory zfsServiceFactory = new ZFSServiceFactoryImpl();
        ZFSService zfsService = zfsServiceFactory.getZFSService();
        SnapshotNamingService snapshotNamingService = new SnapshotNamingServiceImpl();
        StdConsumerFactory stdConsumerFactory = new StdConsumerFactoryImpl(s3Service);
        ReceiveService receiveService = new ReceiveServiceImpl(
                zfsService,
                snapshotNamingService,
                s3Service,
                stdConsumerFactory
        );



        return receiveService;
    }
}
