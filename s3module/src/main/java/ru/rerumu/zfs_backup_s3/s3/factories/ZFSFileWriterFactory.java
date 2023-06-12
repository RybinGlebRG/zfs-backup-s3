package ru.rerumu.zfs_backup_s3.s3.factories;

import ru.rerumu.zfs_backup_s3.s3.utils.ZFSFileWriter;

import java.io.IOException;
import java.nio.file.Path;

public interface ZFSFileWriterFactory {
    ZFSFileWriter getZFSFileWriter(Path path) throws IOException;
}
