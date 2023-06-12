package ru.rerumu.zfs_backup_s3.s3.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

public interface S3CallableFactory {

    Callable<Void> getUploadCallable(Path path, String key);
    Callable<Void> getDownloadCallable(String key, Path path);

    Callable<List<String>> getListCallable(String prefix);
}
