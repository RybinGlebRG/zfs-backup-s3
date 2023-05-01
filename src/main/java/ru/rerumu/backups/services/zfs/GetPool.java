package ru.rerumu.backups.services.zfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.zfs.factories.StdConsumerFactory;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.StdLineConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GetPool implements Callable<Pool> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String poolName;
    private final ProcessFactory processFactory;
    private final ZFSService zfsService;

    private final StdConsumerFactory stdConsumerFactory;


    public GetPool(String poolName, ProcessFactory processFactory, ZFSService zfsService, StdConsumerFactory stdConsumerFactory) {
        this.poolName = poolName;
        this.processFactory = processFactory;
        this.zfsService = zfsService;
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

        processFactory.getProcessWrapper(
                command,
                new StdLineConsumer(logger::error),
                stdConsumerFactory.getDatasetStringStdConsumer(datasetStrings)
        ).call();

        return datasetStrings;
    }

    @Override
    public Pool call() throws Exception {
        List<String> datasetNames = getDatasetNames();

        List<Dataset> datasets = new ArrayList<>();
        for (String name: datasetNames){
            Dataset dataset = zfsService.getDataset(name);
            datasets.add(dataset);
        }

        Pool pool = new Pool(poolName, datasets);
        return pool;
    }
}
