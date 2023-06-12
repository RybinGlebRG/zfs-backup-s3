package ru.rerumu.zfs_backup_s3.backups.services;

public interface ReceiveService {

    void receive(String bucketName, String poolName) throws Exception;
}
