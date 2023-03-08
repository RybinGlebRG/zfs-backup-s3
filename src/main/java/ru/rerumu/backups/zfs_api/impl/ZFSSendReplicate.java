package ru.rerumu.backups.zfs_api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ZFSSendReplicate implements ZFSSend {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProcessWrapper processWrapper;

    public ZFSSendReplicate(Snapshot snapshot, ProcessWrapperFactory processWrapperFactory) throws IOException {
        processWrapper = processWrapperFactory.getProcessWrapper(
                List.of("zfs","send","-vpRPw",snapshot.getFullName())
        );

        processWrapper.run();
        processWrapper.setStderrProcessor(logger::debug);
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
