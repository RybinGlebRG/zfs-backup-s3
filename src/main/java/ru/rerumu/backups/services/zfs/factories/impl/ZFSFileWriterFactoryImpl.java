package ru.rerumu.backups.services.zfs.factories.impl;

import ru.rerumu.backups.services.zfs.ZFSFileWriter;
import ru.rerumu.backups.services.zfs.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.services.zfs.impl.ZFSFileWriterTrivial;

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
