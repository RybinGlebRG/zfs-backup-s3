package ru.rerumu.backups.services.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.services.zfs.ZFSService;
import ru.rerumu.backups.services.zfs.impl.helper.GetDatasetStringStdConsumer;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.StdConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class GetPool implements Callable<Pool> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String poolName;
    private final ProcessFactory processFactory;
    private final ZFSService zfsService;


    public GetPool(String poolName, ProcessFactory processFactory, ZFSService zfsService) {
        this.poolName = poolName;
        this.processFactory = processFactory;
        this.zfsService = zfsService;
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
                new StdConsumer(logger::error),
                new GetDatasetStringStdConsumer(datasetStrings)
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
