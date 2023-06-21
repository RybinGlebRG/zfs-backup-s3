package ru.rerumu.zfs_backup_s3.zfs.callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.utils.CallableOnlyOnce;
import ru.rerumu.zfs_backup_s3.utils.processes.StdLineConsumer;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.impl.StdProcessorImpl;
import ru.rerumu.zfs_backup_s3.zfs.models.Pool;

import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

// TODO: Check thread safe
public class Receive extends CallableOnlyOnce<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Pool pool;
    private final ProcessWrapperFactory processWrapperFactory;
    private final Consumer<BufferedOutputStream> stdinConsumer;

    public Receive(Pool pool, ProcessWrapperFactory processWrapperFactory, Consumer<BufferedOutputStream> stdinConsumer) {
        this.pool = pool;
        this.processWrapperFactory = processWrapperFactory;
        this.stdinConsumer = stdinConsumer;
    }

    @Override
    public Void callOnce() throws Exception {
        List<String> command = new ArrayList<>();
        command.add("zfs");
        command.add("receive");
        command.add("-duvF");
        command.add(pool.name());

        processWrapperFactory.getProcessWrapper(
                command,
                new StdProcessorImpl(
                        new StdLineConsumer(logger::error),
                        new StdLineConsumer(logger::debug),
                        stdinConsumer)
        ).call();

        return null;
    }
}
