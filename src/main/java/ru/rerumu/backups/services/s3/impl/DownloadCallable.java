package ru.rerumu.backups.services.s3.impl;

import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.factories.S3ManagerFactory;
import ru.rerumu.backups.models.S3Storage;

import java.nio.file.Path;
import java.util.concurrent.Callable;

public class DownloadCallable implements Callable<Integer> {
    private final S3ManagerFactory s3ManagerFactory;
    private final Path path;
    private final String key;
    private final S3Storage s3Storage;
    private final S3ClientFactory s3ClientFactory;

    public DownloadCallable(S3ManagerFactory s3ManagerFactory, Path path, String key, S3Storage s3Storage, S3ClientFactory s3ClientFactory) {
        this.s3ManagerFactory = s3ManagerFactory;
        this.path = path;
        this.key = key;
        this.s3Storage = s3Storage;
        this.s3ClientFactory = s3ClientFactory;
    }

    @Override
    public Integer call() throws Exception {
        return null;
    }
}
