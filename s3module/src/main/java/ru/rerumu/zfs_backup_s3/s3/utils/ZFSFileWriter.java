package ru.rerumu.zfs_backup_s3.s3.utils;

import ru.rerumu.zfs_backup_s3.s3.exceptions.FileHitSizeLimitException;
import ru.rerumu.zfs_backup_s3.s3.exceptions.ZFSStreamEndedException;

import java.io.BufferedInputStream;
import java.io.IOException;

// TODO: Rename
public interface ZFSFileWriter extends AutoCloseable {

    void write(BufferedInputStream bufferedInputStream) throws IOException,
            FileHitSizeLimitException,
            ZFSStreamEndedException;
}
