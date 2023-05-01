package ru.rerumu.backups.services.zfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.StdLineConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class CreateSnapshot implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Dataset dataset;
    private final String name;
    private final Boolean isRecursive;
    private final ProcessFactory processFactory;
    private final ExecutorService executorService;


    public CreateSnapshot(Dataset dataset, String name, Boolean isRecursive, ProcessFactory processFactory, ExecutorService executorService) {
        this.dataset = dataset;
        this.name = name;
        this.isRecursive = isRecursive;
        this.processFactory = processFactory;
        this.executorService = executorService;
    }

    @Override
    public Void call() throws Exception {
        List<String> command = new ArrayList<>();
        command.add("zfs");
        command.add("snapshot");
        if (isRecursive){
            command.add("-r");
        }
        command.add(dataset.name()+"@"+name);

        processFactory.getProcessWrapper(
                command,
                new StdLineConsumer(logger::error),
                new StdLineConsumer(logger::debug)
        ).call();

        return null;
    }
}