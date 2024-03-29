package ru.rerumu.zfs_backup_s3.zfs.factories.impl;

import org.checkerframework.checker.nullness.qual.NonNull;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.zfs.callable.*;
import ru.rerumu.zfs_backup_s3.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;
import ru.rerumu.zfs_backup_s3.zfs.factories.ZFSCallableFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@ThreadSafe
public final class ZFSCallableFactoryImpl implements ZFSCallableFactory {
    private final ProcessWrapperFactory processWrapperFactory;
    private final StdConsumerFactory stdConsumerFactory;

    public ZFSCallableFactoryImpl(
            @NonNull ProcessWrapperFactory processWrapperFactory,
            @NonNull StdConsumerFactory stdConsumerFactory) {
        Objects.requireNonNull(processWrapperFactory);
        Objects.requireNonNull(stdConsumerFactory);
        this.processWrapperFactory = processWrapperFactory;
        this.stdConsumerFactory = stdConsumerFactory;
    }

    @Override
    public Callable<Pool> getPoolCallable(String poolName) {
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
