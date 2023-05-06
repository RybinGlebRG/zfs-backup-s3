package ru.rerumu.zfs.factories.impl;

import ru.rerumu.zfs.*;
import ru.rerumu.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs.models.Snapshot;
import ru.rerumu.zfs.models.Dataset;
import ru.rerumu.zfs.models.Pool;
import ru.rerumu.zfs.factories.ZFSCallableFactory;
import ru.rerumu.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.utils.processes.factories.StdProcessorFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class ZFSCallableFactoryImpl implements ZFSCallableFactory {
    private final ProcessWrapperFactory processWrapperFactory;
    private final StdConsumerFactory stdConsumerFactory;

    private final StdProcessorFactory stdProcessorFactory;

    // TODO: Check not null
    public ZFSCallableFactoryImpl(ProcessWrapperFactory processWrapperFactory, StdConsumerFactory stdConsumerFactory, StdProcessorFactory stdProcessorFactory) {
        this.processWrapperFactory = processWrapperFactory;
        this.stdConsumerFactory = stdConsumerFactory;
        this.stdProcessorFactory = stdProcessorFactory;
    }

    @Override
    public Callable<Pool> getPoolCallable(String poolName) {
        // TODO: Thread safe?
        return new GetPool(poolName, processWrapperFactory,this,stdConsumerFactory, stdProcessorFactory);
    }

    @Override
    public Callable<Dataset> getDatasetCallable(String datasetName) {
        return new GetDataset(datasetName, processWrapperFactory, stdProcessorFactory, stdConsumerFactory);
    }

    @Override
    public CreateSnapshot getCreateSnapshotCallable(Dataset dataset, String name, Boolean isRecursive) {
        return new CreateSnapshot(dataset, name, isRecursive, processWrapperFactory,stdProcessorFactory);
    }

    @Override
    public ListSnapshots getListSnapshotsCallable(Dataset dataset) {
        return new ListSnapshots(processWrapperFactory, dataset, stdProcessorFactory,stdConsumerFactory);
    }

    @Override
    public Callable<Void> getSendReplica(
            Snapshot snapshot,
            Consumer<BufferedInputStream> consumer
    ) {
        return new SendReplica(snapshot, processWrapperFactory,consumer,stdProcessorFactory);
    }

    @Override
    public Callable<Void> getReceive(Pool pool, Consumer<BufferedOutputStream> stdinConsumer) {
        return new Receive(pool, processWrapperFactory,stdinConsumer,stdProcessorFactory);
    }
}
