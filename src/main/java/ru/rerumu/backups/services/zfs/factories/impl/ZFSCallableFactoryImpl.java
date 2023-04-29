package ru.rerumu.backups.services.zfs.factories.impl;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.zfs.Dataset;
import ru.rerumu.backups.models.zfs.Pool;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;
import ru.rerumu.backups.services.zfs.impl.*;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZFSCallableFactoryImpl implements ZFSCallableFactory {
    private final ProcessFactory processFactory;
    private final ExecutorService executorService;

    public ZFSCallableFactoryImpl(ProcessFactory processFactory, ExecutorService executorService) {
        this.processFactory = processFactory;
        this.executorService = executorService;
    }

    @Override
    public GetPool getPoolCallable(String poolName) {
        return new GetPool(poolName, processFactory,executorService,this);
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
}
