package ru.rerumu.zfs_backup_s3.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.backups.exceptions.SendError;
import ru.rerumu.zfs_backup_s3.backups.services.S3KeyService;
import ru.rerumu.zfs_backup_s3.backups.services.SendService;
import ru.rerumu.zfs_backup_s3.backups.services.SnapshotNamingService;

import ru.rerumu.zfs_backup_s3.local_storage.services.LocalStorageService;
import ru.rerumu.zfs_backup_s3.s3.S3Service;
import ru.rerumu.zfs_backup_s3.s3.models.Bucket;

import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;
import ru.rerumu.zfs_backup_s3.zfs.ZFSService;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.nio.file.Path;
import java.util.function.Consumer;


// TODO: Use resume tokens?
@NotThreadSafe
public final class SendServiceImpl implements SendService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SnapshotNamingService snapshotNamingService;
    private final ZFSService zfsService;
    private final LocalStorageService localStorageService;
    private final S3Service s3Service;

    public SendServiceImpl(SnapshotNamingService snapshotNamingService, ZFSService zfsService, LocalStorageService localStorageService, S3Service s3Service) {
        this.snapshotNamingService = snapshotNamingService;
        this.zfsService = zfsService;
        this.localStorageService = localStorageService;
        this.s3Service = s3Service;
    }

    @Override
    public void send(Pool pool, Bucket bucket) {
        Snapshot snapshot = zfsService.createRecursiveSnapshot(
                pool.getRootDataset().orElseThrow(),
                snapshotNamingService.generateName()
                );


        String  prefix = S3KeyService.getKey(snapshot.getName(),0);

        Consumer<Path> fileConsumer = path -> s3Service.upload(path,prefix);
        Consumer<BufferedInputStream> bufferedInputStreamConsumer = bufferedInputStream ->
                localStorageService.send(bufferedInputStream, fileConsumer);

        try {
            zfsService.send(
                    snapshot,
                    // TODO: Add test
                    bufferedInputStreamConsumer
            );
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new SendError(e);
        }


    }

    // TODO: Why exception is thrown?
    @Override
    public void send(String poolName, String bucketName) throws Exception {
        try {
            Bucket bucket = new Bucket(bucketName);
            Pool pool = zfsService.getPool(poolName);
            send(pool, bucket);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            throw e;
        }
    }
}
