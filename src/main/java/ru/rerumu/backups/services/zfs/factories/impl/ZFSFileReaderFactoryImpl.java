package ru.rerumu.backups.services.zfs.factories.impl;

import ru.rerumu.backups.services.zfs.ZFSFileReader;
import ru.rerumu.backups.services.zfs.factories.ZFSFileReaderFactory;
import ru.rerumu.backups.services.zfs.impl.ZFSFileReaderTrivial;

import java.io.BufferedOutputStream;
import java.nio.file.Path;

public class ZFSFileReaderFactoryImpl implements ZFSFileReaderFactory {

    @Override
    public ZFSFileReader getZFSFileReader(BufferedOutputStream bufferedOutputStream, Path path) {
        return new ZFSFileReaderTrivial(bufferedOutputStream,path);
    }
}
