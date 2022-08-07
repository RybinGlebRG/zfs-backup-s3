package ru.rerumu.backups.zfs_api;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface ProcessWrapper{
    BufferedInputStream getBufferedInputStream();
    BufferedOutputStream getBufferedOutputStream();
    void close() throws InterruptedException, IOException, ExecutionException;
    void kill() throws InterruptedException, IOException, ExecutionException;

    void run() throws IOException;
    void setStdinProcessor(StdProcessor stdinProcessor);
    void setStderrProcessor(StdProcessor stderrProcessor);

}
