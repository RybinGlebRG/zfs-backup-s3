package ru.rerumu.zfs_backup_s3.s3.impl;

import org.checkerframework.checker.nullness.qual.NonNull;
import ru.rerumu.zfs_backup_s3.s3.factories.S3ClientFactory;
import ru.rerumu.zfs_backup_s3.s3.impl.operations.ListCallable;
import ru.rerumu.zfs_backup_s3.s3.impl.operations.MultipartDownloadCallable;
import ru.rerumu.zfs_backup_s3.s3.impl.operations.MultipartUploadCallable;
import ru.rerumu.zfs_backup_s3.s3.impl.operations.OnepartUploadCallable;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@ThreadSafe
public final class S3CallableFactoryImpl implements S3CallableFactory {
    private final int maxPartSize;
    private final S3RequestService s3RequestService;

    public S3CallableFactoryImpl(
            int maxPartSize,
            @NonNull S3RequestService s3RequestService) {
        Objects.requireNonNull(s3RequestService);
        this.maxPartSize = maxPartSize;
        this.s3RequestService = s3RequestService;
    }

    @Override
    public Callable<Void> getUploadCallable(Path path, String key) {
        try {
            if (Files.size(path) > maxPartSize) {
                return new MultipartUploadCallable(
                        path,
                        key,
                        maxPartSize,
                        s3RequestService
                );
            } else {
                return new OnepartUploadCallable(path, key, s3RequestService);

            }
        } catch (Exception e) {
            throw new AssertionError("Was supposed to not have trouble getting file size", e);
        }
    }

    @Override
    public Callable<Void> getDownloadCallable(String key, Path path) {
        return new MultipartDownloadCallable(path, key,  maxPartSize, s3RequestService);
    }

    @Override
    public Callable<List<String>> getListCallable(String prefix) {
        return new ListCallable(prefix,s3RequestService);
    }
}
