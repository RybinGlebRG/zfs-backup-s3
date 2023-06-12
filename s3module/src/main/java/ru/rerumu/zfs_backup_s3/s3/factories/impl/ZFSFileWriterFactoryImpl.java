package ru.rerumu.zfs_backup_s3.s3.factories.impl;

import ru.rerumu.zfs_backup_s3.s3.utils.ZFSFileWriter;
import ru.rerumu.zfs_backup_s3.s3.factories.ZFSFileWriterFactory;
import ru.rerumu.zfs_backup_s3.s3.utils.impl.ZFSFileWriterTrivial;

import java.io.IOException;
import java.nio.file.Path;

public class ZFSFileWriterFactoryImpl implements ZFSFileWriterFactory {
    private final long filePartSize;

    public ZFSFileWriterFactoryImpl(long filePartSize) {
        this.filePartSize = filePartSize;
    }

    @Override
    public ZFSFileWriter getZFSFileWriter(Path path) throws IOException {
        return new ZFSFileWriterTrivial(filePartSize, path);
    }
}
