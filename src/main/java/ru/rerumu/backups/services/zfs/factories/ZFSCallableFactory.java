package ru.rerumu.backups.services.zfs.factories;

import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.services.zfs.CreateSnapshot;
import ru.rerumu.backups.services.zfs.GetDataset;
import ru.rerumu.backups.services.zfs.GetPool;
import ru.rerumu.backups.services.zfs.ListSnapshots;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.List;
import java.util.concurrent.Callable;

public interface ZFSCallableFactory {

    Callable<Pool> getPoolCallable(String poolName);
    Callable<Dataset> getDatasetCallable(String datasetName);
    Callable<Void> getCreateSnapshotCallable(Dataset dataset, String name, Boolean isRecursive);
    Callable<List<Snapshot>> getListSnapshotsCallable(Dataset dataset);

    Callable<Void> getSendReplica( Snapshot snapshot,
                                   TriConsumer<BufferedInputStream,Runnable,Runnable> consumer);
    Callable<Void> getReceive(Pool pool,TriConsumer<BufferedOutputStream, Runnable, Runnable> stdinConsumer);
}
