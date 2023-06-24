package ru.rerumu.zfs_backup_s3.utils.processes;

import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.processes.impl.StdProcessorImpl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@ThreadSafe
public sealed interface StdProcessor permits StdProcessorMock, StdProcessorImpl {

    void processStd(
            BufferedInputStream stderr,
            BufferedInputStream stdout,
            BufferedOutputStream stdin
    ) throws ExecutionException, InterruptedException, IOException;
}
