package ru.rerumu.backups.services.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.errors.ProcessRunError;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.zfs.Dataset;
import ru.rerumu.backups.models.zfs.Pool;
import ru.rerumu.backups.zfs_api.ProcessWrapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class GetPool implements Callable<Pool> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProcessWrapperFactory processWrapperFactory;
    private final String poolName;

    public GetPool(ProcessWrapperFactory processWrapperFactory, String poolName) {
        if (processWrapperFactory == null || poolName == null){
            throw new IllegalArgumentException();
        }
        this.processWrapperFactory = processWrapperFactory;
        this.poolName = poolName;

    }

    private List<String> getDatasetNames(){
        List<String> command = new ArrayList<>();
        command.add("zfs");
        command.add("list");
        command.add("-rH");
        command.add("-o");
        command.add("name");
        command.add("-s");
        command.add("name");
        command.add(poolName);
        ProcessWrapper processWrapper = processWrapperFactory.getProcessWrapper(command);

        byte[] output;
        try {
            processWrapper.run();
            processWrapper.setStderrProcessor(logger::error);
            output = processWrapper.getBufferedInputStream().readAllBytes();
            processWrapper.close();
        } catch (Exception e){
            throw new ProcessRunError(e);
        }
        String str = new String(output, StandardCharsets.UTF_8);
        logger.debug(String.format("Got from process: \n%s",str));
        String[] lines = str.split("\\n");

        return Arrays.stream(lines)
                .map(String::strip)
                .peek(item -> logger.debug(String.format("Got dataset name: %s",item)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public Pool call() throws Exception {
        List<String> datasetNames = getDatasetNames();
        List<Dataset> datasets = datasetNames.stream()
                .map(name -> {
                    try {
                        return new GetDataset(processWrapperFactory,name).call();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));
        Pool pool = new Pool(poolName, datasets);
        return pool;
    }
}
