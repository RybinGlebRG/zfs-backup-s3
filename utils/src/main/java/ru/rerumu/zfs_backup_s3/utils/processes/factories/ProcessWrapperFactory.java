package ru.rerumu.zfs_backup_s3.utils.processes.factories;

import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.processes.StdProcessor;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.impl.ProcessWrapperFactoryImpl;

import java.util.List;
import java.util.concurrent.Callable;

@ThreadSafe
public sealed interface ProcessWrapperFactory permits ProcessWrapperFactoryImpl {

    Callable<Void> getProcessWrapper(
            List<String> args,
            StdProcessor stdProcessor
    );
}
