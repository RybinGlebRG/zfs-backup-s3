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
import ru.rerumu.zfs_backup_s3.zfs.factories.ZFSCallableFactory;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;


@ThreadSafe
public final class GetPool extends CallableOnlyOnce<Pool> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String poolName;
    private final ProcessWrapperFactory processWrapperFactory;
    private final StdConsumerFactory stdConsumerFactory;
    private final ZFSCallableFactory zfsCallableFactory;

    public GetPool(
            @NonNull String poolName,
            @NonNull ProcessWrapperFactory processWrapperFactory,
            @NonNull ZFSCallableFactory zfsCallableFactory,
            @NonNull StdConsumerFactory stdConsumerFactory
    ) {
        Objects.requireNonNull(poolName);
        Objects.requireNonNull(processWrapperFactory);
        Objects.requireNonNull(zfsCallableFactory);
        Objects.requireNonNull(stdConsumerFactory);
        this.poolName = poolName;
        this.processWrapperFactory = processWrapperFactory;
        this.zfsCallableFactory = zfsCallableFactory;
        this.stdConsumerFactory = stdConsumerFactory;
    }

    private List<String> getDatasetNames() throws Exception {
        List<String> command = new ArrayList<>();
        command.add("zfs");
        command.add("list");
        command.add("-rH");
        command.add("-o");
        command.add("name");
        command.add("-s");
        command.add("name");
        command.add(poolName);

        List<String> datasetStrings = new ArrayList<>();

        processWrapperFactory.getProcessWrapper(
                command,
                new StdProcessorImpl(
                        new StdLineConsumer(logger::error),
                        stdConsumerFactory.getDatasetStringStdConsumer(datasetStrings),
                        null
                )
        ).call();


        return datasetStrings;
    }

    @Override
    protected Pool callOnce() throws Exception {
        List<String> datasetNames = getDatasetNames();

        List<Dataset> datasets = new ArrayList<>();
        for (String name: datasetNames){
            Dataset dataset = zfsCallableFactory.getDatasetCallable(name).call();
            datasets.add(dataset);
        }

        Pool pool = new Pool(poolName, datasets);
        return pool;
    }
}
