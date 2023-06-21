package ru.rerumu.zfs_backup_s3.s3.factories;

import ru.rerumu.zfs_backup_s3.s3.utils.ZFSFileReader;

import java.io.BufferedOutputStream;
import java.nio.file.Path;

public interface ZFSFileReaderFactory {
    ZFSFileReader getZFSFileReader(BufferedOutputStream bufferedOutputStream, Path path);
}