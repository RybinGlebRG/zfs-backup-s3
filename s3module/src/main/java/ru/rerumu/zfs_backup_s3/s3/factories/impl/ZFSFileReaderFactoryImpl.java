package ru.rerumu.zfs_backup_s3.s3.factories.impl;

import ru.rerumu.zfs_backup_s3.s3.utils.ZFSFileReader;
import ru.rerumu.zfs_backup_s3.s3.factories.ZFSFileReaderFactory;
import ru.rerumu.zfs_backup_s3.s3.utils.impl.ZFSFileReaderTrivial;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.io.BufferedOutputStream;
import java.nio.file.Path;

@ThreadSafe
public final class ZFSFileReaderFactoryImpl implements ZFSFileReaderFactory {

    @Override
    public ZFSFileReader getZFSFileReader(BufferedOutputStream bufferedOutputStream, Path path) {
        return new ZFSFileReaderTrivial(bufferedOutputStream,path);
    }
}
