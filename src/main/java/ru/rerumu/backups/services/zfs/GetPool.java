package ru.rerumu.backups.services.zfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.zfs.factories.StdConsumerFactory;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.utils.processes.StdLineConsumer;
import ru.rerumu.backups.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.backups.utils.processes.factories.StdProcessorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GetPool implements Callable<Pool> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String poolName;
    private final ProcessWrapperFactory processWrapperFactory;
    private final StdConsumerFactory stdConsumerFactory;
    private final StdProcessorFactory stdProcessorFactory;

    private final ZFSCallableFactory zfsCallableFactory;

    // TODO: Check not null
    public GetPool(String poolName, ProcessWrapperFactory processWrapperFactory, ZFSCallableFactory zfsCallableFactory, StdConsumerFactory stdConsumerFactory, StdProcessorFactory stdProcessorFactory) {
        this.poolName = poolName;
        this.processWrapperFactory = processWrapperFactory;
        this.zfsCallableFactory = zfsCallableFactory;
        this.stdConsumerFactory = stdConsumerFactory;
        this.stdProcessorFactory = stdProcessorFactory;
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
                stdProcessorFactory.getStdProcessor(
                        new StdLineConsumer(logger::error),
                        stdConsumerFactory.getDatasetStringStdConsumer(datasetStrings)
                )
        ).call();


        return datasetStrings;
    }

    @Override
    public Pool call() throws Exception {
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
