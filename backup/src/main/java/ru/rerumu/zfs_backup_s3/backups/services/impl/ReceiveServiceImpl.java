package ru.rerumu.zfs_backup_s3.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.backups.factories.StdConsumerFactory;
import ru.rerumu.zfs_backup_s3.backups.services.ReceiveService;
import ru.rerumu.zfs_backup_s3.backups.services.SnapshotNamingService;

import ru.rerumu.zfs_backup_s3.s3.S3Service;

import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.ZFSService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class ReceiveServiceImpl implements ReceiveService {
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

    private String getNewestPrefix(String bucketName) {
        String prefix = String.format(
                "%s/",
                bucketName
        );
        List<String> keys = s3Service.list(prefix);

        String maxDateKey = keys.stream()
                .filter(item -> item.matches(bucketName + "/\\w+/level-0/[a-zA-Z0-9:_-]+/.*"))
                .max(Comparator.comparing(
                        item -> snapshotNamingService.extractTime(
                                Paths.get(item).getName(3).toString()
                        )
                ))
                .orElseThrow();
        Path keyPath = Paths.get(maxDateKey);
        String poolName = keyPath.getName(1).toString();
        LocalDateTime maxDate = snapshotNamingService.extractTime(
                keyPath.getName(3).toString()
        );
        String generatedName = snapshotNamingService.generateName(maxDate);


        String res = String.format(
                "%s/%s/level-0/%s/",
                bucketName,
                poolName,
                generatedName
        );
        return res;
    }

    @Override
    public void receive(String bucketName, String poolName) throws Exception {
        try {
            String prefix = getNewestPrefix(bucketName);
            Pool pool = zfsService.getPool(poolName);
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
