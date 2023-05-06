package ru.rerumu.zfs.callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.utils.processes.StdLineConsumer;
import ru.rerumu.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.utils.processes.factories.StdProcessorFactory;
import ru.rerumu.zfs.models.Dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class CreateSnapshot implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Dataset dataset;
    private final String name;
    private final Boolean isRecursive;
    private final ProcessWrapperFactory processWrapperFactory;

    private final StdProcessorFactory stdProcessorFactory;


    public CreateSnapshot(Dataset dataset, String name, Boolean isRecursive, ProcessWrapperFactory processWrapperFactory, StdProcessorFactory stdProcessorFactory) {
        this.dataset = dataset;
        this.name = name;
        this.isRecursive = isRecursive;
        this.processWrapperFactory = processWrapperFactory;
        this.stdProcessorFactory = stdProcessorFactory;
    }

    @Override
    public Void call() throws Exception {
        List<String> command = new ArrayList<>();
        command.add("zfs");
        command.add("snapshot");
        if (isRecursive) {
            command.add("-r");
        }
        command.add(dataset.name() + "@" + name);

        processWrapperFactory.getProcessWrapper(
                command,
                stdProcessorFactory.getStdProcessor(
                        new StdLineConsumer(logger::error),
                        new StdLineConsumer(logger::debug)
                )
        ).call();

        return null;
    }
}
