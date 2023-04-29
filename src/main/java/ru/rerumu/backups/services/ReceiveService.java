package ru.rerumu.backups.services;

import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.models.zfs.Pool;

import java.io.IOException;

public interface ReceiveService {

    void receive(String prefix, ZFSPool zfsPool);

    void receive(String bucketName, String poolName);
}
