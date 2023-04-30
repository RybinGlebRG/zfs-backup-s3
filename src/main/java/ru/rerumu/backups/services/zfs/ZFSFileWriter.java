package ru.rerumu.backups.services.zfs;

import ru.rerumu.backups.exceptions.FileHitSizeLimitException;
import ru.rerumu.backups.exceptions.ZFSStreamEndedException;

import java.io.BufferedInputStream;
import java.io.IOException;

public interface ZFSFileWriter extends AutoCloseable {

    void write(BufferedInputStream bufferedInputStream) throws IOException,
            FileHitSizeLimitException,
            ZFSStreamEndedException;
}
