package ru.rerumu.zfs_backup_s3.zfs.factories;

import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

// TODO: Check thread safe
public interface ZFSCallableFactory {

    Callable<Pool> getPoolCallable(String poolName);
    Callable<Dataset> getDatasetCallable(String datasetName);
    Callable<Void> getCreateSnapshotCallable(Dataset dataset, String name, Boolean isRecursive);
    Callable<List<Snapshot>> getListSnapshotsCallable(Dataset dataset);

    Callable<Void> getSendReplica( Snapshot snapshot,
                                   Consumer<BufferedInputStream> consumer);
    Callable<Void> getReceive(Pool pool,Consumer<BufferedOutputStream> stdinConsumer);
}
