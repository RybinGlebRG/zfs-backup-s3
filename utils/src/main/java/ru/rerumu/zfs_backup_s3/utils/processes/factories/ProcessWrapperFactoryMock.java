package ru.rerumu.zfs_backup_s3.utils.processes.factories;

import ru.rerumu.zfs_backup_s3.utils.processes.StdProcessor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class ProcessWrapperFactoryMock implements ProcessWrapperFactory {
    @Override
    public Callable<Void> getProcessWrapper(List<String> args, StdProcessor stdProcessor) {
        return null;
    }

    @Override
    public Callable<Void> getProcessWrapper(List<String> args, Consumer<BufferedOutputStream> stdinConsumer, Consumer<BufferedInputStream> stdoutConsumer, Consumer<BufferedInputStream> stderrConsumer) {
        return null;
    }
}
