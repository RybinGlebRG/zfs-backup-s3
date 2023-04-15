package ru.rerumu.backups.factories;

import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.s3.S3Manager;
import ru.rerumu.backups.services.s3.impl.ListManager;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Path;

@Deprecated
public interface S3ManagerFactory {

    S3Manager getUploadManager(
            BufferedInputStream bufferedInputStream,
            long size,
            S3Storage s3Storage,
            String key,
            S3Client s3Client
    ) throws IOException;

//    S3Manager getDownloadManager(
//            S3Storage s3Storage,
//            String key,
//            S3Client s3Client,
//            Path tmpDir,
//            Long size,
//            String storedMd5Hex);

    S3Manager getDownloadManager(
            S3Storage s3Storage,
            String key,
            S3Client s3Client,
            Path path);
    S3Manager getDownloadManager(
            S3Storage s3Storage,
            String key,
            S3Client s3Client,
            Path path,
            String storedMd5Hex);

    ListManager getListManager(
            S3Storage s3Storage,
            String key,
            S3Client s3Client);
}
