package ru.rerumu.backups.services.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.errors.ProcessRunError;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.zfs.Dataset;
import ru.rerumu.backups.zfs_api.ProcessWrapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class GetDataset implements Callable<Dataset> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProcessWrapperFactory processWrapperFactory;
    private final String datasetName;

    public GetDataset(ProcessWrapperFactory processWrapperFactory, String datasetName) {
        if (processWrapperFactory == null || datasetName == null){
            throw new IllegalArgumentException();
        }
        this.processWrapperFactory = processWrapperFactory;
        this.datasetName = datasetName;
    }

    private List<Snapshot> getSnapshots(){
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
                .map(Snapshot::new)
                .peek(item -> logger.debug(String.format("Got snapshot: %s",item.getFullName())))
                .collect(Collectors.toCollection(ArrayList::new));
    }


    @Override
    public Dataset call() throws Exception {
        List<Snapshot> snapshots = getSnapshots();
        Dataset dataset = new Dataset(datasetName,snapshots);

        return dataset;
    }
}
