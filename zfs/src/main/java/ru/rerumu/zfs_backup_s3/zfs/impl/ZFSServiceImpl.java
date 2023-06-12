package ru.rerumu.zfs_backup_s3.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.zfs.ZFSService;
import ru.rerumu.zfs_backup_s3.zfs.services.SnapshotService;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.factories.ZFSCallableFactory;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.function.Consumer;

public class ZFSServiceImpl implements ZFSService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ZFSCallableFactory zfsCallableFactory;

    private final SnapshotService snapshotService;

    public ZFSServiceImpl(ZFSCallableFactory zfsCallableFactory, SnapshotService snapshotService) {
        this.zfsCallableFactory = zfsCallableFactory;
        this.snapshotService = snapshotService;
    }

    private void validateSnapshotName(String name){
        if (!name.matches("^[a-zA-Z0-9_-]*$")){
            throw new IllegalArgumentException("Unacceptable snapshot name");
        }
    }

    @Override
    public Pool getPool(String name) throws Exception {
        Pool pool = zfsCallableFactory.getPoolCallable(name).call();
        return pool;

    }

    @Override
    public void send(Snapshot snapshot, Consumer<BufferedInputStream> consumer) throws Exception {
        zfsCallableFactory.getSendReplica(snapshot, consumer).call();
    }

    @Override
    public void receive(Pool pool, Consumer<BufferedOutputStream> consumer) throws Exception {
        zfsCallableFactory.getReceive(pool, consumer).call();
    }

    @Override
    public Snapshot createRecursiveSnapshot(Dataset dataset, String name) {
        validateSnapshotName(name);
        return snapshotService.createRecursiveSnapshot(dataset,name);
    }

}
