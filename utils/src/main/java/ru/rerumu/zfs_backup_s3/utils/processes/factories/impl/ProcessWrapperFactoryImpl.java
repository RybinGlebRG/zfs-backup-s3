package ru.rerumu.zfs_backup_s3.utils.processes.factories.impl;

import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.processes.StdProcessor;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.ProcessWrapper;

import java.util.List;
import java.util.concurrent.Callable;

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
}
