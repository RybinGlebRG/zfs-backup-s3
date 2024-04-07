package ru.rerumu.zfs_backup_s3.zfs.factories;

import ru.rerumu.zfs_backup_s3.utils.Generated;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@Generated
public final class ZFSCallableFactoryMock implements ZFSCallableFactory {
    @Override
    public Callable<Pool> getPoolCallable(String poolName) {
        return null;
    }

    @Override
    public Callable<Dataset> getDatasetCallable(String datasetName) {
        return null;
    }

    @Override
    public Callable<Void> getCreateSnapshotCallable(Dataset dataset, String name, Boolean isRecursive) {
        return null;
    }

    @Override
    public Callable<List<Snapshot>> getListSnapshotsCallable(Dataset dataset) {
        return null;
    }

    @Override
    public Callable<Void> getSendReplica(Snapshot snapshot, Consumer<BufferedInputStream> consumer) {
        return null;
    }

    @Override
    public Callable<Void> getReceive(Pool pool, Consumer<BufferedOutputStream> stdinConsumer) {
        return null;
    }
}
