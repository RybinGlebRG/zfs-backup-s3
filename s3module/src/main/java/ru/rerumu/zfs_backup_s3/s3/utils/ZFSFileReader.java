package ru.rerumu.zfs_backup_s3.s3.utils;

import ru.rerumu.zfs_backup_s3.s3.utils.impl.ZFSFileReaderTrivial;
import ru.rerumu.zfs_backup_s3.utils.NotThreadSafe;

import java.io.IOException;

// TODO: Rename
@NotThreadSafe
public sealed interface ZFSFileReader permits ZFSFileReaderTrivial {

    void read() throws IOException;
}
