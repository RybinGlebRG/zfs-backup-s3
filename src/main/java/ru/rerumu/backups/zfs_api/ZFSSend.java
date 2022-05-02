package ru.rerumu.backups.zfs_api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface ZFSSend {

    BufferedInputStream getBufferedInputStream();
    void close() throws InterruptedException, IOException;
    void kill() throws InterruptedException;
}
