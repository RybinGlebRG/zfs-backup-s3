package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.s3.Bucket;
import ru.rerumu.backups.models.zfs.Pool;
import ru.rerumu.backups.services.s3.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.services.SendService;
import ru.rerumu.backups.services.SnapshotNamingService;
import ru.rerumu.backups.services.SnapshotService;
import ru.rerumu.backups.services.zfs.ZFSService;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;
import ru.rerumu.backups.services.zfs.impl.SendReplica;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.impl.ProcessWrapperImpl;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.zfs.ZFSSend;

import java.util.concurrent.Callable;


// TODO: Use resume tokens?
public class SendServiceImpl implements SendService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ZFSProcessFactory zfsProcessFactory;
    private final S3StreamRepositoryImpl s3StreamRepository;
    private final SnapshotService snapshotService;

    private final SnapshotNamingService snapshotNamingService;
    private final ZFSService zfsService;

    private final ZFSCallableFactory zfsCallableFactory;


    public SendServiceImpl(ZFSProcessFactory zfsProcessFactory, S3StreamRepositoryImpl s3StreamRepository, SnapshotService snapshotService, SnapshotNamingService snapshotNamingService, ZFSService zfsService, ZFSCallableFactory zfsCallableFactory) {
        this.zfsProcessFactory = zfsProcessFactory;
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
                (item,close,kill) -> {
                    try {
                        s3StreamRepository.add(prefix, item);
                    } catch (Exception e) {
                        logger.error(e.getMessage(),e);
                        kill.run();
                        throw new RuntimeException(e);
                    }
                }
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
