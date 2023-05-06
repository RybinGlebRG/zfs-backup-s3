package ru.rerumu.zfs.callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.utils.processes.StdLineConsumer;
import ru.rerumu.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.utils.processes.factories.StdProcessorFactory;
import ru.rerumu.zfs.models.Pool;

import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class Receive implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Pool pool;
    private final ProcessWrapperFactory processWrapperFactory;
    private final Consumer<BufferedOutputStream> stdinConsumer;

    private final StdProcessorFactory stdProcessorFactory;

    public Receive(Pool pool, ProcessWrapperFactory processWrapperFactory, Consumer<BufferedOutputStream> stdinConsumer, StdProcessorFactory stdProcessorFactory) {
        this.pool = pool;
        this.processWrapperFactory = processWrapperFactory;
        this.stdinConsumer = stdinConsumer;
        this.stdProcessorFactory = stdProcessorFactory;
    }

    @Override
    public Void call() throws Exception {
        List<String> command = new ArrayList<>();
        command.add("zfs");
        command.add("receive");
        command.add("-duvF");
        command.add(pool.name());

        processWrapperFactory.getProcessWrapper(
                command,
                stdProcessorFactory.getStdProcessor(
                        new StdLineConsumer(logger::error),
                        new StdLineConsumer(logger::debug),
                        stdinConsumer
                )
        ).call();

        return null;
    }
}