package ru.rerumu.backups.factories.impl;

import ru.rerumu.backups.services.ZFSFileReader;
import ru.rerumu.backups.factories.ZFSFileReaderFactory;
import ru.rerumu.backups.services.impl.ZFSFileReaderTrivial;

import java.io.BufferedOutputStream;
import java.nio.file.Path;

public class ZFSFileReaderFactoryImpl implements ZFSFileReaderFactory {

    @Override
    public ZFSFileReader getZFSFileReader(BufferedOutputStream bufferedOutputStream, Path path) {
        return new ZFSFileReaderTrivial(bufferedOutputStream,path);
    }
}
