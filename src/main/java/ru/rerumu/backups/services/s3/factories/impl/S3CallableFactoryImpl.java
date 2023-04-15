package ru.rerumu.backups.services.s3.factories.impl;

import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.factories.S3ManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.s3.factories.S3CallableFactory;
import ru.rerumu.backups.services.s3.impl.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

public class S3CallableFactoryImpl implements S3CallableFactory {
    private final int maxPartSize;
    private final S3Storage s3Storage;
    private final S3ClientFactory s3ClientFactory;

    public S3CallableFactoryImpl(int maxPartSize, S3Storage s3Storage, S3ClientFactory s3ClientFactory) {
        this.maxPartSize = maxPartSize;
        this.s3Storage = s3Storage;
        this.s3ClientFactory = s3ClientFactory;
    }

    @Override
    public Callable<Void> getUploadCallable(Path path, String key) {
        try {
            if (Files.size(path) > maxPartSize) {
                return new MultipartUploadCallable(path, key, s3Storage, s3ClientFactory, maxPartSize);
            } else {
                return new OnepartUploadCallable(path, key, s3Storage, s3ClientFactory);

            }
        } catch (Exception e){
            throw new AssertionError("Was supposed to not have trouble getting file size",e);
        }
    }

    @Override
    public Callable<Void> getDownloadCallable(String key, Path path) {
        return new MultipartDownloadCallable(path, key, s3Storage, s3ClientFactory, maxPartSize);
    }

    @Override
    public Callable<List<String>> getListCallable(String prefix) {
        return new ListCallable(prefix, s3Storage,s3ClientFactory);
    }
}
