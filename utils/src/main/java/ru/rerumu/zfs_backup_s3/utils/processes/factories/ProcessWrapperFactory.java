package ru.rerumu.zfs_backup_s3.utils.processes.factories;

import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.processes.StdProcessor;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.impl.ProcessWrapperFactoryImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

@ThreadSafe
public sealed interface ProcessWrapperFactory permits ProcessWrapperFactory4Mock, ProcessWrapperFactoryImpl {

    Callable<Void> getProcessWrapper(
            List<String> args,
            StdProcessor stdProcessor
    );

    Callable<Void> getProcessWrapper(
            List<String> args,
            Consumer<BufferedOutputStream> stdinConsumer,
            Consumer<BufferedInputStream> stdoutConsumer,
            Consumer<BufferedInputStream> stderrConsumer
    );
}
