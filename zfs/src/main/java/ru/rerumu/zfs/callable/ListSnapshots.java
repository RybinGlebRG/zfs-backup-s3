package ru.rerumu.zfs.callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.utils.processes.StdLineConsumer;
import ru.rerumu.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.utils.processes.factories.StdProcessorFactory;
import ru.rerumu.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs.models.Snapshot;
import ru.rerumu.zfs.models.Dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ListSnapshots implements Callable<List<Snapshot>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProcessWrapperFactory processWrapperFactory;
    private final Dataset dataset;

    private final StdProcessorFactory stdProcessorFactory;

    private final StdConsumerFactory stdConsumerFactory;

    // TODO: Check not null
    public ListSnapshots(ProcessWrapperFactory processWrapperFactory, Dataset dataset, StdProcessorFactory stdProcessorFactory, StdConsumerFactory stdConsumerFactory) {
        this.processWrapperFactory = processWrapperFactory;
        this.dataset = dataset;
        this.stdProcessorFactory = stdProcessorFactory;
        this.stdConsumerFactory = stdConsumerFactory;
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

        processWrapperFactory.getProcessWrapper(
                command,
                stdProcessorFactory.getStdProcessor(
                        new StdLineConsumer(logger::error),
                        stdConsumerFactory.getSnapshotListStdConsumer(snapshotList)
                )
        ).call();

        return snapshotList;
    }

    @Override
    public List<Snapshot> call() throws Exception {
        List<Snapshot> res = getSnapshots();
        return res;
    }
}
