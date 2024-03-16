package ru.rerumu.zfs_backup_s3.local_storage.services;

import ru.rerumu.zfs_backup_s3.local_storage.services.impl.ZFSFileReaderTrivial;
import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;

import java.io.IOException;

// TODO: Rename
@NotThreadSafe
public sealed interface ZFSFileReader permits ZFSFileReaderTrivial {

    void read() throws IOException;
}
