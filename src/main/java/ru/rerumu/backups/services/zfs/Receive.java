package ru.rerumu.backups.services.zfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.zfs.models.Pool;
import ru.rerumu.backups.utils.processes.ProcessFactory;
import ru.rerumu.backups.utils.processes.StdLineConsumer;
import ru.rerumu.backups.utils.processes.TriConsumer;

import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class Receive implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Pool pool;
    private final ProcessFactory processFactory;
    private final TriConsumer<BufferedOutputStream,Runnable,Runnable> stdinConsumer;

    public Receive(Pool pool, ProcessFactory processFactory, TriConsumer<BufferedOutputStream, Runnable, Runnable> stdinConsumer) {
        this.pool = pool;
        this.processFactory = processFactory;
        this.stdinConsumer = stdinConsumer;
    }

    @Override
    public Void call() throws Exception {
        List<String> command = new ArrayList<>();
        command.add("zfs");
        command.add("receive");
        command.add("-duvF");
        command.add(pool.name());

        processFactory.getProcessWrapper(
                command,
                new StdLineConsumer(logger::error),
                new StdLineConsumer(logger::debug),
                stdinConsumer
        ).call();

        return null;
    }
}
