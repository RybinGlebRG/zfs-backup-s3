package ru.rerumu.zfs_backup_s3.zfs.callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.utils.CallableOnlyOnce;
import ru.rerumu.zfs_backup_s3.utils.processes.StdLineConsumer;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.impl.StdProcessorImpl;
import ru.rerumu.zfs_backup_s3.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs_backup_s3.zfs.factories.ZFSCallableFactory;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


// TODO: Check thread safe
public class GetPool extends CallableOnlyOnce<Pool> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String poolName;
    private final ProcessWrapperFactory processWrapperFactory;
    private final StdConsumerFactory stdConsumerFactory;
    private final ZFSCallableFactory zfsCallableFactory;

    // TODO: Check not null
    public GetPool(String poolName, ProcessWrapperFactory processWrapperFactory, ZFSCallableFactory zfsCallableFactory, StdConsumerFactory stdConsumerFactory) {
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
    public Pool callOnce() throws Exception {
        List<String> datasetNames = getDatasetNames();

        List<Dataset> datasets = new ArrayList<>();
        for (String name: datasetNames){
//            Dataset dataset = zfsService.getDataset(name);
            Dataset dataset = zfsCallableFactory.getDatasetCallable(name).call();
            datasets.add(dataset);
        }

        Pool pool = new Pool(poolName, datasets);
        return pool;
    }
}
