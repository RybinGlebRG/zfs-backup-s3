package ru.rerumu.backups.services.s3.impl;

import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.factories.S3ManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.s3.S3Manager;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

public class UploadCallable implements Callable<Void> {
    private final S3ManagerFactory s3ManagerFactory;
    private final Path path;
    private final String key;
    private final S3Storage s3Storage;
    private final S3ClientFactory s3ClientFactory;

    public UploadCallable(S3ManagerFactory s3ManagerFactory, Path path, String key, S3Storage s3Storage, S3ClientFactory s3ClientFactory) {
        this.s3ManagerFactory = s3ManagerFactory;
        this.path = path;
        this.key = key;
        this.s3Storage = s3Storage;
        this.s3ClientFactory = s3ClientFactory;
    }

    @Override
    public Void call() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path))) {

            S3Manager s3Manager = s3ManagerFactory.getUploadManager(
                    bufferedInputStream,
                    Files.size(path),
                    s3Storage,
                    key,
                    s3ClientFactory.getS3Client(s3Storage)
            );
            s3Manager.run();
        }
        return null;
    }
}
