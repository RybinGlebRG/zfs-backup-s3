package ru.rerumu.backups.services.zfs.factories.impl;

import ru.rerumu.backups.services.zfs.*;
import ru.rerumu.backups.services.zfs.factories.StdConsumerFactory;
import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;
import ru.rerumu.backups.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class ZFSCallableFactoryImpl implements ZFSCallableFactory {
    private final ProcessWrapperFactory processWrapperFactory;
    private final ExecutorService executorService;

    // TODO: Circular dependency?
    private final ZFSService zfsService;

    private final StdConsumerFactory stdConsumerFactory;

    public ZFSCallableFactoryImpl(ProcessWrapperFactory processWrapperFactory, ExecutorService executorService, ZFSService zfsService, StdConsumerFactory stdConsumerFactory) {
        this.processWrapperFactory = processWrapperFactory;
        this.executorService = executorService;
        this.zfsService = zfsService;
        this.stdConsumerFactory = stdConsumerFactory;
    }

    @Override
    public Callable<Pool> getPoolCallable(String poolName) {
        return new GetPool(poolName, processWrapperFactory,zfsService,stdConsumerFactory);
    }

    @Override
    public Callable<Dataset> getDatasetCallable(String datasetName) {
        return new GetDataset(datasetName, processWrapperFactory);
    }

    @Override
    public CreateSnapshot getCreateSnapshotCallable(Dataset dataset, String name, Boolean isRecursive) {
        return new CreateSnapshot(dataset, name, isRecursive, processWrapperFactory,executorService);
    }

    @Override
    public ListSnapshots getListSnapshotsCallable(Dataset dataset) {
        return new ListSnapshots(processWrapperFactory, dataset, executorService);
    }

    @Override
    public Callable<Void> getSendReplica(
            Snapshot snapshot,
            TriConsumer<BufferedInputStream,Runnable,Runnable> consumer
    ) {
        return new SendReplica(snapshot, processWrapperFactory,consumer,executorService);
    }

    @Override
    public Callable<Void> getReceive(Pool pool, TriConsumer<BufferedOutputStream, Runnable, Runnable> stdinConsumer) {
        return new Receive(pool, processWrapperFactory,stdinConsumer);
    }
}
