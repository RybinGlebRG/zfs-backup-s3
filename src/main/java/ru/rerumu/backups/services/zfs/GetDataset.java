package ru.rerumu.backups.services.zfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.zfs.factories.StdConsumerFactory;
import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.utils.processes.StdLineConsumer;
import ru.rerumu.backups.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.backups.utils.processes.factories.StdProcessorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class GetDataset implements Callable<Dataset> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String datasetName;

    private final ProcessWrapperFactory processWrapperFactory;

    private final StdProcessorFactory stdProcessorFactory;
    private final StdConsumerFactory stdConsumerFactory;

    // TODO: Check not null
    public GetDataset(String datasetName, ProcessWrapperFactory processWrapperFactory, StdProcessorFactory stdProcessorFactory, StdConsumerFactory stdConsumerFactory) {
        this.datasetName = datasetName;
        this.processWrapperFactory = processWrapperFactory;
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
        command.add(datasetName);

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
    public Dataset call() throws Exception {
        List<Snapshot> snapshots = getSnapshots();
        Dataset dataset = new Dataset(datasetName,snapshots);

        return dataset;
    }
}
