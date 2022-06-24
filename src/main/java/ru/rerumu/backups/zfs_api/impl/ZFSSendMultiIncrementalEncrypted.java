package ru.rerumu.backups.zfs_api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;

public class ZFSSendMultiIncrementalEncrypted extends ProcessWrapperImpl implements ZFSSend {
    protected final Logger logger = LoggerFactory.getLogger(ZFSSendMultiIncrementalEncrypted.class);

    public ZFSSendMultiIncrementalEncrypted(Snapshot baseSnapshot, Snapshot incrementalSnapshot) throws IOException {
        super(Arrays.asList("zfs", "send", "-vpPIw", baseSnapshot.getFullName(), incrementalSnapshot.getFullName()));

        setStderrProcessor(logger::debug);
    }

    @Override
    public BufferedInputStream getBufferedInputStream() {
        return bufferedInputStream;
    }
}
