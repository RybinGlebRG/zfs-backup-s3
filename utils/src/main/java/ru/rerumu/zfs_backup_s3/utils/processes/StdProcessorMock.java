package ru.rerumu.zfs_backup_s3.utils.processes;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public final class StdProcessorMock implements StdProcessor {
    @Override
    public void processStd(BufferedInputStream stderr, BufferedInputStream stdout, BufferedOutputStream stdin) throws ExecutionException, InterruptedException, IOException {

    }
}
