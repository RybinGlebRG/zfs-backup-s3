package ru.rerumu.backups.services.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.zfs.Dataset;
import ru.rerumu.backups.models.zfs.Pool;
import ru.rerumu.backups.services.zfs.factories.ZFSCallableFactory;
import ru.rerumu.backups.services.zfs.impl.helper.GetDatasetStringStdConsumer;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.ProcessWrapper;
import ru.rerumu.backups.utils.processes.StdConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class GetPool implements Callable<Pool> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String poolName;

    private final ProcessFactory processFactory;

    private final ExecutorService executorService;
    private final ZFSCallableFactory zfsCallableFactory;


    public GetPool(String poolName, ProcessFactory processFactory, ExecutorService executorService, ZFSCallableFactory zfsCallableFactory) {
        this.poolName = poolName;
        this.processFactory = processFactory;
        this.executorService = executorService;
        this.zfsCallableFactory = zfsCallableFactory;
    }

    private List<String> getDatasetNames() throws ExecutionException, InterruptedException {
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

        ProcessWrapper processWrapper = processFactory.getProcessWrapper(
                command,
                new StdConsumer(logger::error),
                new GetDatasetStringStdConsumer(datasetStrings)
        );
        executorService.submit(processWrapper).get();

        return datasetStrings;
    }

    @Override
    public Pool call() throws Exception {
        List<String> datasetNames = getDatasetNames();

        List<Future<Dataset>> futureList = datasetNames.stream()
                .map(item -> executorService.submit(zfsCallableFactory.getDatasetCallable(item)))
                .collect(Collectors.toCollection(ArrayList::new));

        List<Dataset> datasets = new ArrayList<>();
        for(Future<Dataset> item: futureList){
            datasets.add(item.get());
        }

        Pool pool = new Pool(poolName, datasets);
        return pool;
    }
}
