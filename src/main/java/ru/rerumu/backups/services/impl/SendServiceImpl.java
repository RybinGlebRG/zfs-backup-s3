package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.services.s3.models.Bucket;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.services.zfs.impl.consumer.SendStdoutConsumer;
import ru.rerumu.backups.services.s3.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.services.SendService;
import ru.rerumu.backups.services.zfs.SnapshotNamingService;
import ru.rerumu.backups.services.zfs.SnapshotService;
import ru.rerumu.backups.services.zfs.ZFSService;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;

import java.util.concurrent.Callable;


// TODO: Use resume tokens?
public class SendServiceImpl implements SendService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final S3StreamRepositoryImpl s3StreamRepository;
    private final SnapshotService snapshotService;

    private final SnapshotNamingService snapshotNamingService;
    private final ZFSService zfsService;

    private final ZFSCallableFactory zfsCallableFactory;


    public SendServiceImpl(S3StreamRepositoryImpl s3StreamRepository, SnapshotService snapshotService, SnapshotNamingService snapshotNamingService, ZFSService zfsService, ZFSCallableFactory zfsCallableFactory) {
        this.s3StreamRepository = s3StreamRepository;
        this.snapshotService = snapshotService;
        this.snapshotNamingService = snapshotNamingService;
        this.zfsService = zfsService;
        this.zfsCallableFactory = zfsCallableFactory;
    }

    private String escapeSymbols(String srcString) {
        return srcString.replace('/', '-');
    }

    @Override
    public void send(Pool pool, Bucket bucket) {
        Snapshot snapshot = snapshotService.createRecursiveSnapshot(
                pool.getRootDataset().orElseThrow(),
                snapshotNamingService.generateName()
                );
        String prefix = String.format(
                "%s/%s/level-0/%s/",
                bucket.name(),
                escapeSymbols(pool.name()),
                escapeSymbols(snapshot.getName())
        );

        Callable<Void> sendReplica = zfsCallableFactory.getSendReplica(
                snapshot,
                new SendStdoutConsumer(s3StreamRepository,prefix)
        );

        try {
            sendReplica.call();
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new SendError(e);
        }


//        try (ZFSSend zfsSend = zfsProcessFactory.getZFSSendReplicate(snapshot)){
//            String prefix = String.format(
//                    "%s/%s/level-0/%s/",
//                    bucket.name(),
//                    escapeSymbols(pool.name()),
//                    escapeSymbols(snapshot.getName())
//            );
//            try {
//                s3StreamRepository.add(prefix, zfsSend.getBufferedInputStream());
//            } catch (Exception e){
//                zfsSend.kill();
//                throw e;
//            }
//        } catch (Exception e){
//            logger.error(e.getMessage(),e);
//            throw new SendError(e);
//        }
    }

    @Override
    public void send(String poolName, String bucketName) {
        Bucket bucket = new Bucket(bucketName);
        Pool pool = zfsService.getPool(poolName);
        send(pool,bucket);
    }
}
