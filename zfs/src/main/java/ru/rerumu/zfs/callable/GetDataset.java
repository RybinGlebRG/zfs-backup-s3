package ru.rerumu.zfs.callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.utils.processes.StdLineConsumer;
import ru.rerumu.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.utils.processes.impl.StdProcessorImpl;
import ru.rerumu.zfs.factories.StdConsumerFactory;
import ru.rerumu.zfs.models.Snapshot;
import ru.rerumu.zfs.models.Dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class GetDataset implements Callable<Dataset> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String datasetName;

    private final ProcessWrapperFactory processWrapperFactory;

    private final StdConsumerFactory stdConsumerFactory;

    // TODO: Check not null
    public GetDataset(String datasetName, ProcessWrapperFactory processWrapperFactory, StdConsumerFactory stdConsumerFactory) {
        this.datasetName = datasetName;
        this.processWrapperFactory = processWrapperFactory;
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
        command.add(datasetName);

        List<Snapshot> snapshotList = new ArrayList<>();

        processWrapperFactory.getProcessWrapper(
                command,
                new StdProcessorImpl(
                        new StdLineConsumer(logger::error),
                        stdConsumerFactory.getSnapshotListStdConsumer(snapshotList),
                        null
                )
        ).call();

        return snapshotList;
    }


    @Override
    public Dataset call() throws Exception {
        List<Snapshot> snapshots = getSnapshots();
        Dataset dataset = new Dataset(datasetName,snapshots);

        return dataset;
    }
}
