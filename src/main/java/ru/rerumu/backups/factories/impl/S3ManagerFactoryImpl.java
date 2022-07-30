package ru.rerumu.backups.factories.impl;

import ru.rerumu.backups.factories.S3ManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.S3Manager;
import ru.rerumu.backups.services.impl.*;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class S3ManagerFactoryImpl implements S3ManagerFactory {
    private final int maxPartSize;

    public S3ManagerFactoryImpl(int maxPartSize) {
        this.maxPartSize = maxPartSize;
    }

    @Override
    public S3Manager getUploadManager(BufferedInputStream bufferedInputStream, long size, S3Storage s3Storage, String key, S3Client s3Client)
            throws IOException {
        if (size > maxPartSize) {
            return new MultipartUploadManager(bufferedInputStream, size, s3Storage, key, s3Client, maxPartSize);
        } else {
            return new OnepartUploadManager(bufferedInputStream, s3Storage, key, s3Client);
        }
    }

//    @Override
//    public S3Manager getDownloadManager(S3Storage s3Storage, String key, S3Client s3Client, Path path, Long size, String storedMd5Hex) {
//        if (size != null && size > maxPartSize) {
//            return new MultipartDownloadManager(s3Storage, key, s3Client, path, maxPartSize);
//        } else {
//            return new OnepartDownloadManager(s3Storage, key, s3Client, path);
//        }
//    }

    @Override
    public S3Manager getDownloadManager(S3Storage s3Storage, String key, S3Client s3Client, Path path) {
        return new MultipartDownloadManager(s3Storage, key, s3Client, path, maxPartSize);
    }

    @Override
    public ListManager getListManager(S3Storage s3Storage, String key, S3Client s3Client) {
        return new ListManager(s3Storage, key, s3Client);
    }
}
