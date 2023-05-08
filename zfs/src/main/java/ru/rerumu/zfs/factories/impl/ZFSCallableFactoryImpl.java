package ru.rerumu.zfs.factories.impl;

import ru.rerumu.zfs.callable.*;
import ru.rerumu.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs.models.Snapshot;
import ru.rerumu.zfs.models.Dataset;
import ru.rerumu.zfs.models.Pool;
import ru.rerumu.zfs.factories.ZFSCallableFactory;
import ru.rerumu.utils.processes.factories.ProcessWrapperFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class ZFSCallableFactoryImpl implements ZFSCallableFactory {
    private final ProcessWrapperFactory processWrapperFactory;
    private final StdConsumerFactory stdConsumerFactory;

    // TODO: Check not null
    public ZFSCallableFactoryImpl(ProcessWrapperFactory processWrapperFactory, StdConsumerFactory stdConsumerFactory) {
        this.processWrapperFactory = processWrapperFactory;
        this.stdConsumerFactory = stdConsumerFactory;
    }

    @Override
    public Callable<Pool> getPoolCallable(String poolName) {
        // TODO: Thread safe?
        return new GetPool(poolName, processWrapperFactory,this,stdConsumerFactory);
    }

    @Override
    public Callable<Dataset> getDatasetCallable(String datasetName) {
        return new GetDataset(datasetName, processWrapperFactory, stdConsumerFactory);
    }

    @Override
    public CreateSnapshot getCreateSnapshotCallable(Dataset dataset, String name, Boolean isRecursive) {
        return new CreateSnapshot(dataset, name, isRecursive, processWrapperFactory);
    }

    @Override
    public ListSnapshots getListSnapshotsCallable(Dataset dataset) {
        return new ListSnapshots(processWrapperFactory, dataset, stdConsumerFactory);
    }

    @Override
    public Callable<Void> getSendReplica(
            Snapshot snapshot,
            Consumer<BufferedInputStream> consumer
    ) {
        return new SendReplica(snapshot, processWrapperFactory,consumer);
    }

    @Override
    public Callable<Void> getReceive(Pool pool, Consumer<BufferedOutputStream> stdinConsumer) {
        return new Receive(pool, processWrapperFactory,stdinConsumer);
    }
}
