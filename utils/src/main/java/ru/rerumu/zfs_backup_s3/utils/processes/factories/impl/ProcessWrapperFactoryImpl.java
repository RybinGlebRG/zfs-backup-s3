package ru.rerumu.zfs_backup_s3.utils.processes.factories.impl;

import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.processes.StdProcessor;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.ProcessWrapper;
import ru.rerumu.zfs_backup_s3.utils.processes.impl.StdProcessorImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@ThreadSafe
public final class ProcessWrapperFactoryImpl implements ProcessWrapperFactory {
    private final ProcessFactory processFactory;

    public ProcessWrapperFactoryImpl(ProcessFactory processFactory) {
        this.processFactory = processFactory;
    }
    @Override
    public synchronized Callable<Void> getProcessWrapper(List<String> args, StdProcessor stdProcessor) {
        return new ProcessWrapper(args,processFactory,stdProcessor);
    }

    @Override
    public Callable<Void> getProcessWrapper(List<String> args, Consumer<BufferedOutputStream> stdinConsumer, Consumer<BufferedInputStream> stdoutConsumer, Consumer<BufferedInputStream> stderrConsumer) {
        return new ProcessWrapper(
                args,
                processFactory,
                new StdProcessorImpl(stderrConsumer, stdoutConsumer, stdinConsumer)
        );
    }
}
