package ru.rerumu.backups.factories.impl;

import ru.rerumu.backups.services.ZFSFileReader;
import ru.rerumu.backups.factories.ZFSFileReaderFactory;
import ru.rerumu.backups.services.impl.ZFSFileReaderFull;

import java.io.BufferedOutputStream;
import java.nio.file.Path;

public class ZFSFileReaderFactoryImpl implements ZFSFileReaderFactory {
    private final String password;

    public ZFSFileReaderFactoryImpl(String password){
        this.password = password;
    }

    @Override
    public ZFSFileReader getZFSFileReader(BufferedOutputStream bufferedOutputStream, Path path) {
        return new ZFSFileReaderFull(bufferedOutputStream,path,password);
    }
}
