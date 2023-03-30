package ru.rerumu.backups.zfs_api.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.ThreadCloseError;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.zfs.ZFSReceive;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;

public class ZFSReceiveImpl implements ZFSReceive {
    private final Logger logger = LoggerFactory.getLogger(ZFSReceiveImpl.class);

    private final ProcessWrapper processWrapper;

    public ZFSReceiveImpl(String pool, ProcessWrapperFactory processWrapperFactory) throws IOException {
        processWrapper = processWrapperFactory.getProcessWrapper(
                List.of("zfs", "receive", "-duvF", pool)
        );

        processWrapper.run();
        processWrapper.setStderrProcessor(logger::error);
        processWrapper.setStdinProcessor(logger::debug);
    }

    public BufferedOutputStream getBufferedOutputStream() {
        return processWrapper.getBufferedOutputStream();
    }

    @Override
    public void close() {
        try {
            processWrapper.close();
        } catch (Exception e){
            throw new ThreadCloseError(e);
        }
    }

}
