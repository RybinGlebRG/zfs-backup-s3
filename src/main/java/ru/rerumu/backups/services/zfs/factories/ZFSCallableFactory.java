package ru.rerumu.backups.services.zfs.factories;

import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.services.zfs.impl.CreateSnapshot;
import ru.rerumu.backups.services.zfs.impl.GetDataset;
import ru.rerumu.backups.services.zfs.impl.GetPool;
import ru.rerumu.backups.services.zfs.impl.ListSnapshots;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.concurrent.Callable;

public interface ZFSCallableFactory {

    GetPool getPoolCallable(String poolName);
    GetDataset getDatasetCallable(String datasetName);
    CreateSnapshot getCreateSnapshotCallable(Dataset dataset, String name, Boolean isRecursive);
    ListSnapshots getListSnapshotsCallable(Dataset dataset);

    Callable<Void> getSendReplica( Snapshot snapshot,
                                   TriConsumer<BufferedInputStream,Runnable,Runnable> consumer);
    Callable<Void> getReceive(Pool pool,TriConsumer<BufferedOutputStream, Runnable, Runnable> stdinConsumer);
}
