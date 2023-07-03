package ru.rerumu.zfs_backup_s3.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.backups.factories.StdConsumerFactory;
import ru.rerumu.zfs_backup_s3.backups.services.ReceiveService;
import ru.rerumu.zfs_backup_s3.backups.services.S3KeyService;
import ru.rerumu.zfs_backup_s3.backups.services.SnapshotNamingService;

import ru.rerumu.zfs_backup_s3.s3.S3Service;

import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.ZFSService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@NotThreadSafe
public final class ReceiveServiceImpl implements ReceiveService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ZFSService zfsService;
    private final SnapshotNamingService snapshotNamingService;

    private final S3Service s3Service;

    private final StdConsumerFactory stdConsumerFactory;

    public ReceiveServiceImpl(ZFSService zfsService, SnapshotNamingService snapshotNamingService, S3Service s3Service, StdConsumerFactory stdConsumerFactory) {
        this.zfsService = zfsService;
        this.snapshotNamingService = snapshotNamingService;
        this.s3Service = s3Service;
        this.stdConsumerFactory = stdConsumerFactory;
    }

    private String getNewestPrefix(int level) {
        String prefix = S3KeyService.getKey(level);
        List<String> keys = s3Service.list(prefix);

        logger.info(String.format("Found keys: %s",keys));

        Optional<LocalDateTime> maxDate = S3KeyService.parseAndGetMaxDate(keys, level);

        String res = S3KeyService.getKey(maxDate.orElseThrow(), level);

        return res;
    }

    @Override
    public void receive(String bucketName, String targetPoolName) throws Exception {
        try {
            String prefix = getNewestPrefix(0);
            Pool pool = zfsService.getPool(targetPoolName);
            zfsService.receive(
                    pool,
                    // TODO: Test
                    stdConsumerFactory.getReceiveStdinConsumer(prefix)
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }

    }
}
