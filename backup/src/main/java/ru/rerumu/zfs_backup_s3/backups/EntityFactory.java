package ru.rerumu.zfs_backup_s3.backups;

import ru.rerumu.zfs_backup_s3.backups.services.ReceiveService;
import ru.rerumu.zfs_backup_s3.backups.services.SendService;
import ru.rerumu.zfs_backup_s3.backups.services.impl.ReceiveServiceImpl;
import ru.rerumu.zfs_backup_s3.backups.services.impl.SendServiceImpl;
import ru.rerumu.zfs_backup_s3.local_storage.factories.ZFSFileReaderFactory;
import ru.rerumu.zfs_backup_s3.local_storage.factories.ZFSFileWriterFactory;
import ru.rerumu.zfs_backup_s3.local_storage.factories.impl.ZFSFileReaderFactoryImpl;
import ru.rerumu.zfs_backup_s3.local_storage.factories.impl.ZFSFileWriterFactoryImpl;
import ru.rerumu.zfs_backup_s3.local_storage.services.LocalStorageService;
import ru.rerumu.zfs_backup_s3.local_storage.services.impl.ConsecutiveLocalStorageService;
import ru.rerumu.zfs_backup_s3.s3.S3ServiceFactory;
import ru.rerumu.zfs_backup_s3.s3.S3ServiceFactoryImpl;
import ru.rerumu.zfs_backup_s3.s3.S3Service;
import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import ru.rerumu.zfs_backup_s3.backups.services.SnapshotNamingService;
import ru.rerumu.zfs_backup_s3.utils.FileManager;
import ru.rerumu.zfs_backup_s3.utils.impl.FileManagerImpl;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.zfs.ZFSService;
import ru.rerumu.zfs_backup_s3.zfs.ZFSServiceFactory;
import ru.rerumu.zfs_backup_s3.backups.services.impl.SnapshotNamingServiceImpl;
import ru.rerumu.zfs_backup_s3.zfs.ZFSServiceFactoryImpl;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.file.Path;
import java.util.UUID;

@ThreadSafe
public final class EntityFactory {

    private final Region region;
    private final String keyId;
    private final String secretKey;
    private final Path prefix;
    private final URI endpoint;
    private final String storageClass;
    private final int maxPartSize;
    private final long filePartSize;
    private final Path tempDir;

    public EntityFactory(Region region, String keyId, String secretKey, Path prefix, URI endpoint, String storageClass, int maxPartSize, long filePartSize, Path tempDir) {
        this.region = region;
        this.keyId = keyId;
        this.secretKey = secretKey;
        this.prefix = prefix;
        this.endpoint = endpoint;
        this.storageClass = storageClass;
        this.maxPartSize = maxPartSize;
        this.filePartSize = filePartSize;
        this.tempDir = tempDir;
    }

    public SendService getSendService(
            String bucketName
    ) {
        S3Storage s3Storage = new S3Storage(region, bucketName, keyId, secretKey, prefix, endpoint, storageClass);
        S3ServiceFactory s3ServiceFactory =  new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                maxPartSize
        );
        SnapshotNamingService snapshotNamingService = new SnapshotNamingServiceImpl();
        ZFSServiceFactory zfsServiceFactory = new ZFSServiceFactoryImpl();
        ZFSService zfsService = zfsServiceFactory.getZFSService();
        ZFSFileReaderFactory zfsFileReaderFactory = new ZFSFileReaderFactoryImpl();
        ZFSFileWriterFactory zfsFileWriterFactory = new ZFSFileWriterFactoryImpl(filePartSize);
        FileManager fileManager = new FileManagerImpl(UUID.randomUUID().toString(), tempDir);
        LocalStorageService localStorageService = new ConsecutiveLocalStorageService(
                zfsFileReaderFactory,
                zfsFileWriterFactory,
                fileManager
        );
        SendService sendService = new SendServiceImpl(
                snapshotNamingService,
                zfsService,
                localStorageService,
                s3Service);
        return sendService;
    }

    public ReceiveService getReceiveService(
            String bucketName
    ) {
        S3Storage s3Storage = new S3Storage(region, bucketName, keyId, secretKey, prefix, endpoint, storageClass);
        S3ServiceFactory s3ServiceFactory =  new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                maxPartSize
        );
        ZFSServiceFactory zfsServiceFactory = new ZFSServiceFactoryImpl();
        ZFSService zfsService = zfsServiceFactory.getZFSService();
        ZFSFileReaderFactory zfsFileReaderFactory = new ZFSFileReaderFactoryImpl();
        ZFSFileWriterFactory zfsFileWriterFactory = new ZFSFileWriterFactoryImpl(filePartSize);
        FileManager fileManager = new FileManagerImpl(UUID.randomUUID().toString(), tempDir);
        LocalStorageService localStorageService = new ConsecutiveLocalStorageService(
                zfsFileReaderFactory,
                zfsFileWriterFactory,
                fileManager
        );
        ReceiveService receiveService = new ReceiveServiceImpl(
                zfsService,
                s3Service,
                localStorageService
        );
        return receiveService;
    }
}
