package ru.rerumu.backups.zfs_api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface ZFSGetDatasetProperty {
    BufferedInputStream getBufferedInputStream();
    void close() throws InterruptedException, IOException, ExecutionException;
}
