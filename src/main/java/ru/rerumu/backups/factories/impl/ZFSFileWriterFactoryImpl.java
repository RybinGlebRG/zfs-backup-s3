package ru.rerumu.backups.factories.impl;

import ru.rerumu.backups.services.ZFSFileWriter;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.services.impl.ZFSFileWriterTrivial;

public class ZFSFileWriterFactoryImpl implements ZFSFileWriterFactory {
    private final int chunkSize;
    private final long filePartSize;

    public ZFSFileWriterFactoryImpl(int chunkSize, long filePartSize){
        this.chunkSize = chunkSize;
        this.filePartSize = filePartSize;
    }

    @Override
    public ZFSFileWriter getZFSFileWriter() {
        return new ZFSFileWriterTrivial(chunkSize,filePartSize);
    }
}
