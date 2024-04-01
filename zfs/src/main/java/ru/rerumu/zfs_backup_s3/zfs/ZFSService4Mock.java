package ru.rerumu.zfs_backup_s3.zfs;

import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.function.Consumer;

public final class ZFSService4Mock implements ZFSService {
    @Override
    public Pool getPool(String name) throws Exception {
        return null;
    }

    @Override
    public void send(Snapshot snapshot, Consumer<BufferedInputStream> consumer) throws Exception {

    }

    @Override
    public void receive(Pool pool, Consumer<BufferedOutputStream> consumer) throws Exception {

    }

    @Override
    public Snapshot createRecursiveSnapshot(Dataset dataset, String name) {
        return null;
    }
}
