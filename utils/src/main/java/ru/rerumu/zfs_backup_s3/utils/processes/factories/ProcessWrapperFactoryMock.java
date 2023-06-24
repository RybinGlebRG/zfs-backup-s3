package ru.rerumu.zfs_backup_s3.utils.processes.factories;

import ru.rerumu.zfs_backup_s3.utils.processes.StdProcessor;

import java.util.List;
import java.util.concurrent.Callable;

public final class ProcessWrapperFactoryMock implements ProcessWrapperFactory {
    @Override
    public Callable<Void> getProcessWrapper(List<String> args, StdProcessor stdProcessor) {
        return null;
    }
}
