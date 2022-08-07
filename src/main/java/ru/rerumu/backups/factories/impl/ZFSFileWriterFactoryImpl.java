package ru.rerumu.backups.factories.impl;

import ru.rerumu.backups.services.ZFSFileWriter;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.services.impl.ZFSFileWriterTrivial;

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
