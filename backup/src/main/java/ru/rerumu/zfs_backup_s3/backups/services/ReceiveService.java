package ru.rerumu.zfs_backup_s3.backups.services;

import ru.rerumu.zfs_backup_s3.backups.services.impl.ReceiveServiceImpl;
import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;

@NotThreadSafe
public sealed interface ReceiveService permits ReceiveServiceMock, ReceiveServiceImpl {

    void receive(String bucketName, String targetPoolName) throws Exception;
}
