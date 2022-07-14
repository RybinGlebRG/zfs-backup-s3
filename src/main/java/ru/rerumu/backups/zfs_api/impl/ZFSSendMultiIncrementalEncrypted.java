package ru.rerumu.backups.zfs_api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ZFSSendMultiIncrementalEncrypted  implements ZFSSend {
    protected final Logger logger = LoggerFactory.getLogger(ZFSSendMultiIncrementalEncrypted.class);

    private final ProcessWrapper processWrapper;

    public ZFSSendMultiIncrementalEncrypted(
            Snapshot baseSnapshot,
            Snapshot incrementalSnapshot,
            ProcessWrapperFactory processWrapperFactory)
            throws IOException {
//        super(Arrays.asList("zfs", "send", "-vpPIw", baseSnapshot.getFullName(), incrementalSnapshot.getFullName()));

//        setStderrProcessor(logger::debug);

        processWrapper = processWrapperFactory.getProcessWrapper(
                List.of("zfs", "send", "-vpPIw", baseSnapshot.getFullName(), incrementalSnapshot.getFullName())
        );

        processWrapper.setStderrProcessor(logger::error);

        processWrapper.run();
    }

    @Override
    public BufferedInputStream getBufferedInputStream() {
        return processWrapper.getBufferedInputStream();
    }

    @Override
    public void close() throws InterruptedException, IOException, ExecutionException {
        processWrapper.close();
    }

    @Override
    public void kill() throws InterruptedException, IOException, ExecutionException {
        processWrapper.kill();
    }
}
