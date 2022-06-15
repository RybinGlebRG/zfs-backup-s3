package ru.rerumu.backups.zfs_api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.zfs_api.ZFSReceive;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class ZFSReceiveImpl extends ProcessWrapperImpl implements ZFSReceive {
    protected final Logger logger = LoggerFactory.getLogger(ZFSReceiveImpl.class);

    public ZFSReceiveImpl(String pool) throws IOException {
        super(Arrays.asList("zfs","receive","-duvF",pool));

        setStderrProcessor(logger::error);
        setStdinProcessor(logger::debug);
    }

    public BufferedOutputStream getBufferedOutputStream() {
        return bufferedOutputStream;
    }

}
