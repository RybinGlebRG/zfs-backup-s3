package ru.rerumu.zfs_backup_s3.s3.impl;

import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@ThreadSafe
public sealed interface S3CallableFactory permits S3CallableFactoryImpl {

    Callable<Void> getUploadCallable(Path path, String key);
    Callable<Void> getDownloadCallable(String key, Path path);

    Callable<List<String>> getListCallable(String prefix);
}
