package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.factories.ZFSProcessFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.s3.Bucket;
import ru.rerumu.backups.models.zfs.Pool;
import ru.rerumu.backups.repositories.impl.S3StreamRepositoryImpl;
import ru.rerumu.backups.services.SendService;
import ru.rerumu.backups.services.SnapshotNamingService;
import ru.rerumu.backups.services.SnapshotService;
import ru.rerumu.backups.zfs_api.zfs.ZFSSend;


// TODO: Use resume tokens?
public class SendServiceImpl implements SendService {

    private final static String SNAPSHOT_PREFIX="zfs-backup-s3";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ZFSProcessFactory zfsProcessFactory;
    private final S3StreamRepositoryImpl s3StreamRepository;
    private final SnapshotService snapshotService;

    private final SnapshotNamingService snapshotNamingService;


    public SendServiceImpl(ZFSProcessFactory zfsProcessFactory, S3StreamRepositoryImpl s3StreamRepository, SnapshotService snapshotService, SnapshotNamingService snapshotNamingService) {
        this.zfsProcessFactory = zfsProcessFactory;
        this.s3StreamRepository = s3StreamRepository;
        this.snapshotService = snapshotService;
        this.snapshotNamingService = snapshotNamingService;
    }

    private String escapeSymbols(String srcString) {
        return srcString.replace('/', '-');
    }

    @Override
    public void send(Pool pool, Bucket bucket) {
        Snapshot snapshot = snapshotService.createRecursiveSnapshot(
                pool.getRootDataset().orElseThrow(),
                snapshotNamingService.getNameWithCurrentTime(SNAPSHOT_PREFIX)
                );
        try (ZFSSend zfsSend = zfsProcessFactory.getZFSSendReplicate(snapshot)){
            String prefix = String.format(
                    "%s/%s/level-0/%s",
                    bucket.name(),
                    escapeSymbols(pool.name()),
                    escapeSymbols(snapshot.getName())
            );
            try {
                s3StreamRepository.add(prefix, zfsSend.getBufferedInputStream());
            } catch (Exception e){
                zfsSend.kill();
                throw e;
            }
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new SendError(e);
        }
    }
}
