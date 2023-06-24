package ru.rerumu.zfs_backup_s3.zfs.callable;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.utils.CallableOnlyOnce;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.processes.StdLineConsumer;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.impl.StdProcessorImpl;
import ru.rerumu.zfs_backup_s3.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@ThreadSafe
public final class ListSnapshots extends CallableOnlyOnce<List<Snapshot>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProcessWrapperFactory processWrapperFactory;
    private final Dataset dataset;
    private final StdConsumerFactory stdConsumerFactory;

    public ListSnapshots(
            @NonNull ProcessWrapperFactory processWrapperFactory,
            @NonNull Dataset dataset,
            @NonNull StdConsumerFactory stdConsumerFactory) {
        Objects.requireNonNull(processWrapperFactory);
        Objects.requireNonNull(dataset);
        Objects.requireNonNull(stdConsumerFactory);
        this.processWrapperFactory = processWrapperFactory;
        this.dataset = dataset;
        this.stdConsumerFactory = stdConsumerFactory;
    }

    private List<Snapshot> getSnapshots() throws Exception {
        List<String> command = new ArrayList<>();
        command.add("zfs");
        command.add("list");
        command.add("-rH");
        command.add("-t");
        command.add("snapshot");
        command.add("-o");
        command.add("name");
        command.add("-s");
        command.add("creation");
        command.add("-d");
        command.add("1");
        command.add(dataset.name());

        List<Snapshot> snapshotList = new ArrayList<>();

        processWrapperFactory.getProcessWrapper(
                command,
                new StdProcessorImpl(
                        new StdLineConsumer(logger::error),
                        stdConsumerFactory.getSnapshotListStdConsumer(snapshotList),
                        null
                )
        ).call();

        return snapshotList;
    }

    @Override
    protected List<Snapshot> callOnce() throws Exception {
        List<Snapshot> res = getSnapshots();
        return res;
    }
}
