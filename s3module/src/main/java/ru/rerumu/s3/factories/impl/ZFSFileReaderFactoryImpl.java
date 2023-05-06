package ru.rerumu.s3.factories.impl;

import ru.rerumu.s3.utils.ZFSFileReader;
import ru.rerumu.s3.factories.ZFSFileReaderFactory;
import ru.rerumu.s3.utils.impl.ZFSFileReaderTrivial;

import java.io.BufferedOutputStream;
import java.nio.file.Path;

public class ZFSFileReaderFactoryImpl implements ZFSFileReaderFactory {

    @Override
    public ZFSFileReader getZFSFileReader(BufferedOutputStream bufferedOutputStream, Path path) {
        return new ZFSFileReaderTrivial(bufferedOutputStream,path);
    }
}
