package ru.rerumu.zfs_backup_s3.zfs.callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.StdLineConsumer;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.impl.StdProcessorImpl;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;


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


    // TODO: Check not null
    public SendReplica(Snapshot snapshot, ProcessWrapperFactory processWrapperFactory, Consumer<BufferedInputStream> stdoutConsumer) {
        this.snapshot = snapshot;
        this.processWrapperFactory = processWrapperFactory;
        this.stdoutConsumer = stdoutConsumer;
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
                new StdProcessorImpl(
                        new StdLineConsumer(logger::debug),
                        stdoutConsumer,
                        null
                )
        ).call();

        return null;
    }
}
