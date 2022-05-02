package ru.rerumu.backups.zfs_api;

import java.io.BufferedInputStream;
import java.io.IOException;

public interface ProcessWrapper {
    BufferedInputStream getBufferedInputStream();
    void close() throws InterruptedException, IOException;
}
