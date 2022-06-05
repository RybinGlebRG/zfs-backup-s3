package ru.rerumu.backups.io.impl;

import ru.rerumu.backups.io.ZFSFileReader;
import ru.rerumu.backups.io.ZFSFileReaderFactory;

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
