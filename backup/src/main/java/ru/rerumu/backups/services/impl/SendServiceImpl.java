package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.consumers.SendStdoutConsumer;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.services.SendService;
import ru.rerumu.backups.services.SnapshotNamingService;

import ru.rerumu.s3.models.Bucket;
import ru.rerumu.s3.repositories.impl.S3StreamRepositoryImpl;

import ru.rerumu.zfs.ZFSService;
import ru.rerumu.zfs.models.Pool;
import ru.rerumu.zfs.models.Snapshot;


// TODO: Use resume tokens?
public class SendServiceImpl implements SendService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final S3StreamRepositoryImpl s3StreamRepository;
    private final SnapshotNamingService snapshotNamingService;
    private final ZFSService zfsService;


    public SendServiceImpl(S3StreamRepositoryImpl s3StreamRepository, SnapshotNamingService snapshotNamingService, ZFSService zfsService) {
        this.s3StreamRepository = s3StreamRepository;
        this.snapshotNamingService = snapshotNamingService;
        this.zfsService = zfsService;
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
                    // TODO: Add factory to add ability to test?
                    new SendStdoutConsumer(s3StreamRepository,prefix)
            );
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new SendError(e);
        }


    }

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
