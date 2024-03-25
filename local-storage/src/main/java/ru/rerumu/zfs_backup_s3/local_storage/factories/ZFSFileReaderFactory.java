package ru.rerumu.zfs_backup_s3.local_storage.factories;


import ru.rerumu.zfs_backup_s3.local_storage.factories.impl.ZFSFileReaderFactoryImpl;
import ru.rerumu.zfs_backup_s3.local_storage.services.ZFSFileReader;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.io.BufferedOutputStream;
import java.nio.file.Path;

@ThreadSafe
public interface ZFSFileReaderFactory {
    ZFSFileReader getZFSFileReader(BufferedOutputStream bufferedOutputStream, Path path);
}
