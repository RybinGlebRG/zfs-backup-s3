package ru.rerumu.backups.zfs_api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.ZFSReceive;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class ZFSReceiveImpl implements ZFSReceive {
    private final Logger logger = LoggerFactory.getLogger(ZFSReceiveImpl.class);

    private final ProcessWrapper processWrapper;

    public ZFSReceiveImpl(String pool, ProcessWrapperFactory processWrapperFactory) throws IOException {
//        super(Arrays.asList("zfs","receive","-duvF",pool));
        processWrapper = processWrapperFactory.getProcessWrapper(
                Arrays.asList("zfs", "receive", "-duvF", pool)
        );

        processWrapper.setStderrProcessor(logger::error);
        processWrapper.setStdinProcessor(logger::debug);

        processWrapper.run();
    }

    public BufferedOutputStream getBufferedOutputStream() {
        return processWrapper.getBufferedOutputStream();
    }

    @Override
    public void close() throws InterruptedException, IOException, ExecutionException {
        processWrapper.close();
    }

}
