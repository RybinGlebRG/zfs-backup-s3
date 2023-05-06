package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.ReceiveService;
import ru.rerumu.backups.services.SnapshotNamingService;
import ru.rerumu.backups.consumers.ReceiveStdinConsumer;

import ru.rerumu.s3.repositories.impl.S3StreamRepositoryImpl;

import ru.rerumu.zfs.models.Pool;
import ru.rerumu.zfs.ZFSService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public class ReceiveServiceImpl implements ReceiveService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final S3StreamRepositoryImpl s3StreamRepository;
    private final ZFSService zfsService;
    private final SnapshotNamingService snapshotNamingService;    ;

    public ReceiveServiceImpl(S3StreamRepositoryImpl s3StreamRepository, ZFSService zfsService, SnapshotNamingService snapshotNamingService) {
        this.s3StreamRepository = s3StreamRepository;
        this.zfsService = zfsService;
        this.snapshotNamingService = snapshotNamingService;
    }

    private String getNewestPrefix(String bucketName) {
        String prefix = String.format(
                "%s/",
                bucketName
        );
        List<String> keys = s3StreamRepository.listAll(prefix);

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
                    new ReceiveStdinConsumer(s3StreamRepository, prefix)
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }

    }
}
