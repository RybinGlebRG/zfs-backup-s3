package ru.rerumu.backups.services.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.errors.ProcessRunError;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.models.zfs.Dataset;
import ru.rerumu.backups.services.zfs.impl.helper.ListSnapshotStdConsumer;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.ProcessWrapper;
import ru.rerumu.backups.utils.processes.StdConsumer;
import ru.rerumu.backups.utils.processes.StdProcessor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ListSnapshots implements Callable<List<Snapshot>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProcessFactory processFactory;
    private final Dataset dataset;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ListSnapshots(ProcessFactory processFactory, Dataset dataset) {
        if (dataset == null || processFactory == null){
            throw new IllegalArgumentException();
        }
        this.processFactory = processFactory;
        this.dataset = dataset;
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
        command.add(dataset.name());

        List<Snapshot> snapshotList = new ArrayList<>();

        ProcessWrapper processWrapper = processFactory.getProcessWrapper(
                command,
                new StdConsumer(logger::error),
                new ListSnapshotStdConsumer(dataset,snapshotList)
        );

        try {
            executorService.submit(processWrapper).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return snapshotList;
    }

    @Override
    public List<Snapshot> call() throws Exception {
        List<Snapshot> res = getSnapshots();
        return res;
    }
}
