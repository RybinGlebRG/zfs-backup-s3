package ru.rerumu.zfs_backup_s3.zfs.callable;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.units.qual.N;
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
import java.util.concurrent.Callable;

@ThreadSafe
public class GetDataset extends CallableOnlyOnce<Dataset> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String datasetName;

    private final ProcessWrapperFactory processWrapperFactory;

    private final StdConsumerFactory stdConsumerFactory;

    public GetDataset(
            @Nullable String datasetName,
            @NonNull ProcessWrapperFactory processWrapperFactory,
            @NonNull StdConsumerFactory stdConsumerFactory
    ) {
        Objects.requireNonNull(processWrapperFactory,"processWrapperFactory cannot be null");
        Objects.requireNonNull(stdConsumerFactory,"stdConsumerFactory cannot be null");
        this.datasetName = datasetName;
        this.processWrapperFactory = processWrapperFactory;
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
        command.add(datasetName);

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
    protected Dataset callOnce() throws Exception {
        List<Snapshot> snapshots = getSnapshots();
        Dataset dataset = new Dataset(datasetName,snapshots);

        return dataset;
    }
}
