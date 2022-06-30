package ru.rerumu.backups.factories.impl;

import ru.rerumu.backups.factories.UploadManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.UploadManager;
import ru.rerumu.backups.services.impl.MultipartUploadManager;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UploadManagerFactoryImpl implements UploadManagerFactory {
    private final static Long FILE_SIZE_FOR_MULTIPART=104_857_600L;

    @Override
    public UploadManager getUploadManager(Path file, S3Storage s3Storage, String key, S3Client s3Client) throws IOException {
        if (Files.size(file) > FILE_SIZE_FOR_MULTIPART){
            return new MultipartUploadManager(file, s3Storage, key, s3Client);
        } else {
            return null;
        }
    }
}
