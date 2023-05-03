package ru.rerumu.backups.services.zfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.backups.utils.processes.StdLineConsumer;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class SendReplica implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Snapshot snapshot;
    private final ProcessWrapperFactory processWrapperFactory;

    private final TriConsumer<BufferedInputStream,Runnable,Runnable> consumer;

    private final ExecutorService executorService;

    public SendReplica(Snapshot snapshot, ProcessWrapperFactory processWrapperFactory, TriConsumer<BufferedInputStream,Runnable,Runnable> consumer, ExecutorService executorService) {
        this.snapshot = snapshot;
        this.processWrapperFactory = processWrapperFactory;
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

        processWrapperFactory.getProcessWrapper(
                command,
                new StdLineConsumer(logger::debug),
                consumer
        ).call();

        return null;
    }
}
