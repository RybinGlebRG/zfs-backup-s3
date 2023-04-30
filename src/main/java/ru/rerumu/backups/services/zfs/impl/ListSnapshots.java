package ru.rerumu.backups.services.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.impl.helper.SnapshotListStdConsumer;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.StdConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ListSnapshots implements Callable<List<Snapshot>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProcessFactory processFactory;
    private final Dataset dataset;
    private final ExecutorService executorService;


    public ListSnapshots(ProcessFactory processFactory, Dataset dataset, ExecutorService executorService) {
        this.processFactory = processFactory;
        this.dataset = dataset;
        this.executorService = executorService;
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
        command.add(dataset.name());

        List<Snapshot> snapshotList = new ArrayList<>();

        processFactory.getProcessWrapper(
                command,
                new StdConsumer(logger::error),
                new SnapshotListStdConsumer(snapshotList)
        ).call();
        return snapshotList;
    }

    @Override
    public List<Snapshot> call() throws Exception {
        List<Snapshot> res = getSnapshots();
        return res;
    }
}
