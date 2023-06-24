package ru.rerumu.zfs_backup_s3.s3.factories;

import ru.rerumu.zfs_backup_s3.s3.factories.impl.ZFSFileWriterFactoryImpl;
import ru.rerumu.zfs_backup_s3.s3.utils.ZFSFileWriter;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.io.IOException;
import java.nio.file.Path;

@ThreadSafe
public sealed interface ZFSFileWriterFactory permits ZFSFileWriterFactoryImpl {
    ZFSFileWriter getZFSFileWriter(Path path) throws IOException;
}
