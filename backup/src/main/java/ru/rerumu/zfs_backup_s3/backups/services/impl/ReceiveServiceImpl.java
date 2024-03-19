package ru.rerumu.zfs_backup_s3.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.backups.services.ReceiveService;
import ru.rerumu.zfs_backup_s3.backups.services.S3KeyService;

import ru.rerumu.zfs_backup_s3.local_storage.services.LocalStorageService;
import ru.rerumu.zfs_backup_s3.s3.S3Service;

import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.ZFSService;

import java.io.BufferedOutputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NotThreadSafe
public final class ReceiveServiceImpl implements ReceiveService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ZFSService zfsService;

    private final S3Service s3Service;
    private final LocalStorageService localStorageService;

    public ReceiveServiceImpl(
            ZFSService zfsService,
            S3Service s3Service,
            LocalStorageService localStorageService
    ) {
        this.zfsService = zfsService;
        this.s3Service = s3Service;
        this.localStorageService = localStorageService;
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

            Consumer<BufferedOutputStream> outputStreamGenerator = bufferedOutputStream ->
                    localStorageService.receive(prefix, bufferedOutputStream);

            Pool pool = zfsService.getPool(targetPoolName);
            zfsService.receive(
                    pool,
                    // TODO: Test
                    outputStreamGenerator
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        }

    }
}
