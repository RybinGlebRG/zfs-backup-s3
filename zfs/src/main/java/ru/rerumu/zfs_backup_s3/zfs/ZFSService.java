package ru.rerumu.zfs_backup_s3.zfs;

import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;
import ru.rerumu.zfs_backup_s3.zfs.impl.ZFSServiceImpl;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.function.Consumer;

@NotThreadSafe
public sealed interface ZFSService permits ZFSServiceImpl {

    Pool getPool(String name) throws Exception;
    void send(Snapshot snapshot, Consumer<BufferedInputStream> consumer) throws Exception;

    void receive(Pool pool, Consumer<BufferedOutputStream> consumer) throws Exception;

    Snapshot createRecursiveSnapshot(Dataset dataset, String name);
}
