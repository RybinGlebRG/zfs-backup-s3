package ru.rerumu.zfs_backup_s3.local_storage.services;

import ru.rerumu.zfs_backup_s3.local_storage.exceptions.FileHitSizeLimitException;
import ru.rerumu.zfs_backup_s3.local_storage.exceptions.ZFSStreamEndedException;
import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;

import java.io.BufferedInputStream;
import java.io.IOException;

// TODO: Rename
@NotThreadSafe
public interface ZFSFileWriter extends AutoCloseable {

    void write(BufferedInputStream bufferedInputStream) throws IOException,
            FileHitSizeLimitException,
            ZFSStreamEndedException;
}
