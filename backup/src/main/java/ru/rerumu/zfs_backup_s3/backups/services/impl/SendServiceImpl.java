package ru.rerumu.zfs_backup_s3.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.backups.exceptions.SendError;
import ru.rerumu.zfs_backup_s3.backups.factories.StdConsumerFactory;
import ru.rerumu.zfs_backup_s3.backups.services.SendService;
import ru.rerumu.zfs_backup_s3.backups.services.SnapshotNamingService;

import ru.rerumu.zfs_backup_s3.s3.models.Bucket;

import ru.rerumu.zfs_backup_s3.zfs.ZFSService;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;


// TODO: Use resume tokens?
public class SendServiceImpl implements SendService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SnapshotNamingService snapshotNamingService;
    private final ZFSService zfsService;
    private final StdConsumerFactory stdConsumerFactory;


    public SendServiceImpl(SnapshotNamingService snapshotNamingService, ZFSService zfsService, StdConsumerFactory stdConsumerFactory) {
        this.snapshotNamingService = snapshotNamingService;
        this.zfsService = zfsService;
        this.stdConsumerFactory = stdConsumerFactory;
    }

    private String escapeSymbols(String srcString) {
        return srcString.replace('/', '-');
    }

    @Override
    public void send(Pool pool, Bucket bucket) {
        Snapshot snapshot = zfsService.createRecursiveSnapshot(
                pool.getRootDataset().orElseThrow(),
                snapshotNamingService.generateName()
                );
        String prefix = String.format(
                "%s/%s/level-0/%s/",
                bucket.name(),
                escapeSymbols(pool.name()),
                escapeSymbols(snapshot.getName())
        );

        try {
            zfsService.send(
                    snapshot,
                    // TODO: Add test
                    stdConsumerFactory.getSendStdoutConsumer(prefix)
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
