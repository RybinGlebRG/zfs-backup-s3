package ru.rerumu.backups.services.zfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.zfs.models.Snapshot;
import ru.rerumu.backups.utils.processes.StdLineConsumer;
import ru.rerumu.backups.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.backups.utils.processes.factories.StdProcessorFactory;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class SendReplica implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Snapshot snapshot;
    private final ProcessWrapperFactory processWrapperFactory;

    private final Consumer<BufferedInputStream> stdoutConsumer;


    private final StdProcessorFactory stdProcessorFactory;

    // TODO: Check not null
    public SendReplica(Snapshot snapshot, ProcessWrapperFactory processWrapperFactory, Consumer<BufferedInputStream> stdoutConsumer, StdProcessorFactory stdProcessorFactory) {
        this.snapshot = snapshot;
        this.processWrapperFactory = processWrapperFactory;
        this.stdoutConsumer = stdoutConsumer;
        this.stdProcessorFactory = stdProcessorFactory;
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
                stdProcessorFactory.getStdProcessor(
                        new StdLineConsumer(logger::debug),
                        stdoutConsumer
                )
        ).call();

        return null;
    }
}
