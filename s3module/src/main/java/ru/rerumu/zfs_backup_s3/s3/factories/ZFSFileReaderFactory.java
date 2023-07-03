package ru.rerumu.zfs_backup_s3.s3.factories;

import ru.rerumu.zfs_backup_s3.s3.factories.impl.ZFSFileReaderFactoryImpl;
import ru.rerumu.zfs_backup_s3.s3.utils.ZFSFileReader;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.io.BufferedOutputStream;
import java.nio.file.Path;

@ThreadSafe
public sealed interface ZFSFileReaderFactory permits ZFSFileReaderFactoryImpl {
    ZFSFileReader getZFSFileReader(BufferedOutputStream bufferedOutputStream, Path path);
}
