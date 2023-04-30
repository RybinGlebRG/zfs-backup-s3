package ru.rerumu.backups.services;

import ru.rerumu.backups.services.s3.models.Bucket;
import ru.rerumu.backups.services.zfs.models.Pool;

public interface SendService {

    void send(Pool pool, Bucket bucket);
    void send(String poolName, String bucketName);
}
