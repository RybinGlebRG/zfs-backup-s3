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
import java.util.concurrent.ExecutionException;

public class ZFSSendFull implements ZFSSend {
    protected final Logger logger = LoggerFactory.getLogger(ZFSSendFull.class);

    private final ProcessWrapper processWrapper;

    public ZFSSendFull(Snapshot fullSnapshot, ProcessWrapperFactory processWrapperFactory) throws IOException {
//        super(Arrays.asList("zfs","send","-vpP",fullSnapshot.getFullName()));

        processWrapper = processWrapperFactory.getProcessWrapper(
                Arrays.asList("zfs","send","-vpP",fullSnapshot.getFullName())
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
