package ru.rerumu.zfs_backup_s3.backups.services;

import ru.rerumu.zfs_backup_s3.s3.models.Bucket;
import ru.rerumu.zfs_backup_s3.utils.Generated;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;

@Generated
public final class SendService4Mock implements SendService {
    @Override
    public void send(Pool pool, Bucket bucket, String continueSnapshotName) {

    }

    @Override
    public void send(String poolName, String bucketName, String continueSnapshotName) throws Exception {

    }
}
