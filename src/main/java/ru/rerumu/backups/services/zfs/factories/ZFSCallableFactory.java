package ru.rerumu.backups.services.zfs.factories;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.zfs.Dataset;
import ru.rerumu.backups.models.zfs.Pool;
import ru.rerumu.backups.services.zfs.impl.CreateSnapshot;
import ru.rerumu.backups.services.zfs.impl.GetDataset;
import ru.rerumu.backups.services.zfs.impl.GetPool;
import ru.rerumu.backups.services.zfs.impl.ListSnapshots;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.concurrent.Callable;

public interface ZFSCallableFactory {

    GetPool getPoolCallable(String poolName);
    GetDataset getDatasetCallable(String datasetName);
    CreateSnapshot getCreateSnapshotCallable(Dataset dataset, String name, Boolean isRecursive);
    ListSnapshots getListSnapshotsCallable(Dataset dataset);

    Callable<Void> getSendReplica( Snapshot snapshot,
                                   TriConsumer<BufferedInputStream,Runnable,Runnable> consumer);
}
