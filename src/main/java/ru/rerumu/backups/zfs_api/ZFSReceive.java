package ru.rerumu.backups.zfs_api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface ZFSReceive extends AutoCloseable {

    BufferedOutputStream getBufferedOutputStream();
    void close();
}
