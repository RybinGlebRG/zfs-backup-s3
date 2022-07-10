package ru.rerumu.backups.factories.impl;

import ru.rerumu.backups.factories.UploadManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.UploadManager;
import ru.rerumu.backups.services.impl.MultipartUploadManager;
import ru.rerumu.backups.services.impl.OnepartUploadManager;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UploadManagerFactoryImpl implements UploadManagerFactory {
    private final int maxPartSize;

    public UploadManagerFactoryImpl(int maxPartSize){
        this.maxPartSize = maxPartSize;
    }

    @Override
    public UploadManager getUploadManager(BufferedInputStream bufferedInputStream, long size, S3Storage s3Storage, String key, S3Client s3Client)
            throws IOException {
        if (size > maxPartSize){
            return new MultipartUploadManager(bufferedInputStream, size, s3Storage, key, s3Client, maxPartSize);
        } else {
            return new OnepartUploadManager(bufferedInputStream, s3Storage, key, s3Client);
        }
    }
}
