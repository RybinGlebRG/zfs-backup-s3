package ru.rerumu.zfs_backup_s3.backups.services;

import ru.rerumu.zfs_backup_s3.s3.models.Bucket;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;

public final class SendServiceMock implements SendService {
    @Override
    public void send(Pool pool, Bucket bucket) {

    }

    @Override
    public void send(String poolName, String bucketName) throws Exception {

    }
}
