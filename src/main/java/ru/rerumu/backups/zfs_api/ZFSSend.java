package ru.rerumu.backups.zfs_api;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public interface ZFSSend extends AutoCloseable{

    BufferedInputStream getBufferedInputStream();
    void close();
    void kill();
}
