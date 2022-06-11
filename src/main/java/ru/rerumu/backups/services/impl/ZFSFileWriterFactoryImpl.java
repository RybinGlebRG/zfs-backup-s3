package ru.rerumu.backups.services.impl;

import ru.rerumu.backups.services.ZFSFileWriter;
import ru.rerumu.backups.services.ZFSFileWriterFactory;

public class ZFSFileWriterFactoryImpl implements ZFSFileWriterFactory {
    private final String password;
    private final int chunkSize;
    private final long filePartSize;

    public ZFSFileWriterFactoryImpl(String password, int chunkSize, long filePartSize){
        this.password = password;
        this.chunkSize = chunkSize;
        this.filePartSize = filePartSize;
    }

    @Override
    public ZFSFileWriter getZFSFileWriter() {
        return new ZFSFileWriterFull(password,chunkSize,filePartSize);
    }
}
