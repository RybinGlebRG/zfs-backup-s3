package ru.rerumu.zfs_backup_s3.local_storage.factories;

import ru.rerumu.zfs_backup_s3.local_storage.factories.impl.ZFSFileWriterFactoryImpl;
import ru.rerumu.zfs_backup_s3.local_storage.services.ZFSFileWriter;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.io.IOException;
import java.nio.file.Path;

@ThreadSafe
public interface ZFSFileWriterFactory {
    ZFSFileWriter getZFSFileWriter(Path path) throws IOException;
}
