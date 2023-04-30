package ru.rerumu.backups.services.zfs.factories.impl;

import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.services.zfs.ZFSService;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;
import ru.rerumu.backups.services.zfs.impl.*;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class ZFSCallableFactoryImpl implements ZFSCallableFactory {
    private final ProcessFactory processFactory;
    private final ExecutorService executorService;

    // TODO: Circular dependency?
    private final ZFSService zfsService;

    public ZFSCallableFactoryImpl(ProcessFactory processFactory, ExecutorService executorService, ZFSService zfsService) {
        this.processFactory = processFactory;
        this.executorService = executorService;
        this.zfsService = zfsService;
    }

    @Override
    public GetPool getPoolCallable(String poolName) {
        return new GetPool(poolName, processFactory,zfsService);
    }

    @Override
    public GetDataset getDatasetCallable(String datasetName) {
        return new GetDataset(datasetName, processFactory);
    }

    @Override
    public CreateSnapshot getCreateSnapshotCallable(Dataset dataset, String name, Boolean isRecursive) {
        return new CreateSnapshot(dataset, name, isRecursive, processFactory,executorService);
    }

    @Override
    public ListSnapshots getListSnapshotsCallable(Dataset dataset) {
        return new ListSnapshots(processFactory, dataset, executorService);
    }

    @Override
    public Callable<Void> getSendReplica(
            Snapshot snapshot,
            TriConsumer<BufferedInputStream,Runnable,Runnable> consumer
    ) {
        return new SendReplica(snapshot,processFactory,consumer,executorService);
    }

    @Override
    public Callable<Void> getReceive(Pool pool, TriConsumer<BufferedOutputStream, Runnable, Runnable> stdinConsumer) {
        return new Receive(pool,processFactory,stdinConsumer);
    }
}
