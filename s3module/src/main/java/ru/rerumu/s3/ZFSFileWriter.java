package ru.rerumu.s3;

import ru.rerumu.s3.exceptions.FileHitSizeLimitException;
import ru.rerumu.s3.exceptions.ZFSStreamEndedException;

import java.io.BufferedInputStream;
import java.io.IOException;

// TODO: Rename
public interface ZFSFileWriter extends AutoCloseable {

    void write(BufferedInputStream bufferedInputStream) throws IOException,
            FileHitSizeLimitException,
            ZFSStreamEndedException;
}
