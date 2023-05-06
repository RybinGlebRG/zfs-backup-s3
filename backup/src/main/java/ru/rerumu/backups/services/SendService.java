package ru.rerumu.backups.services;

import ru.rerumu.s3.models.Bucket;
import ru.rerumu.zfs.models.Pool;

public interface SendService {

    void send(Pool pool, Bucket bucket);
    void send(String poolName, String bucketName) throws Exception;
}
