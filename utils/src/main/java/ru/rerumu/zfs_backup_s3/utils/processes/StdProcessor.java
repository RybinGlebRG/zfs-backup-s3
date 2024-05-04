package ru.rerumu.zfs_backup_s3.utils.processes;

import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.processes.impl.StdProcessorImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@ThreadSafe
public sealed interface StdProcessor permits StdProcessor4Mock, StdProcessorImpl {

    void processStd(
            BufferedInputStream stderr,
            BufferedInputStream stdout,
            BufferedOutputStream stdin
    ) throws ExecutionException, InterruptedException, IOException;
}
