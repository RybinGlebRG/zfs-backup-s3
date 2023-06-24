package ru.rerumu.zfs_backup_s3.backups.services;

import ru.rerumu.zfs_backup_s3.backups.services.impl.SendServiceImpl;
import ru.rerumu.zfs_backup_s3.s3.models.Bucket;
import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;

@NotThreadSafe
public sealed interface SendService permits SendServiceImpl {

    void send(Pool pool, Bucket bucket);
    void send(String poolName, String bucketName) throws Exception;
}
