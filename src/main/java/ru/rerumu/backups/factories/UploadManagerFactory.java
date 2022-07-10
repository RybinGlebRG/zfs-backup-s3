package ru.rerumu.backups.factories;

import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.UploadManager;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Path;

public interface UploadManagerFactory {

    UploadManager getUploadManager(
            BufferedInputStream bufferedInputStream,
            long size,
            S3Storage s3Storage,
            String key,
            S3Client s3Client
    ) throws IOException;
}
