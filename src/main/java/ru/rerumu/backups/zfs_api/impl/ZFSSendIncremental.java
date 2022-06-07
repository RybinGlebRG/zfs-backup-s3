package ru.rerumu.backups.zfs_api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.zfs_api.InputStreamLogger;
import ru.rerumu.backups.zfs_api.StderrLogger;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;

public class ZFSSendIncremental extends ProcessWrapperImpl implements ZFSSend {
    protected final Logger logger = LoggerFactory.getLogger(ZFSSendIncremental.class);

    public ZFSSendIncremental(Snapshot baseSnapshot, Snapshot incrementalSnapshot) throws IOException {
        super(Arrays.asList("zfs", "send", "-vpPi", baseSnapshot.getFullName(), incrementalSnapshot.getFullName()));

        futureList.add(executorService.submit(new StderrLogger(bufferedErrorStream, LoggerFactory.getLogger(StderrLogger.class))));
    }

    @Override
    public BufferedInputStream getBufferedInputStream() {
        return bufferedInputStream;
    }
}
