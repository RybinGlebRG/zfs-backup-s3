package ru.rerumu.backups.services.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.services.zfs.impl.helper.GetDatasetStringStdConsumer;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.ProcessWrapper;
import ru.rerumu.backups.utils.processes.StdConsumer;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class SendReplica implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Snapshot snapshot;
    private final ProcessFactory processFactory;

    private final TriConsumer<BufferedInputStream,Runnable,Runnable> consumer;

    private final ExecutorService executorService;

    public SendReplica(Snapshot snapshot, ProcessFactory processFactory, TriConsumer<BufferedInputStream,Runnable,Runnable> consumer, ExecutorService executorService) {
        this.snapshot = snapshot;
        this.processFactory = processFactory;
        this.consumer = consumer;
        this.executorService = executorService;
    }

    @Override
    public Void call() throws Exception {
        List<String> command = new ArrayList<>();
        command.add("zfs");
        command.add("send");
        command.add("-vpRPw");
        command.add(snapshot.getFullName());

        processFactory.getProcessWrapper(
                command,
                new StdConsumer(logger::debug),
                consumer
        ).call();

        return null;
    }
}
