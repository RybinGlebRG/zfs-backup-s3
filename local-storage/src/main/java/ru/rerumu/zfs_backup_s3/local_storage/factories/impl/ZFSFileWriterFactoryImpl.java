package ru.rerumu.zfs_backup_s3.local_storage.factories.impl;

import ru.rerumu.zfs_backup_s3.local_storage.factories.ZFSFileWriterFactory;
import ru.rerumu.zfs_backup_s3.local_storage.services.ZFSFileWriter;
import ru.rerumu.zfs_backup_s3.local_storage.services.impl.ZFSFileWriterTrivial;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.io.IOException;
import java.nio.file.Path;

@ThreadSafe
public final class ZFSFileWriterFactoryImpl implements ZFSFileWriterFactory {
    private final long filePartSize;

    public ZFSFileWriterFactoryImpl(long filePartSize) {
        this.filePartSize = filePartSize;
    }

    @Override
    public ZFSFileWriter getZFSFileWriter(Path path) throws IOException {
        return new ZFSFileWriterTrivial(filePartSize, path);
    }
}
