package ru.rerumu.zfs_backup_s3.local_storage.factories.impl;

import ru.rerumu.zfs_backup_s3.local_storage.factories.ZFSFileReaderFactory;
import ru.rerumu.zfs_backup_s3.local_storage.services.ZFSFileReader;
import ru.rerumu.zfs_backup_s3.local_storage.services.impl.ZFSFileReaderTrivial;
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
