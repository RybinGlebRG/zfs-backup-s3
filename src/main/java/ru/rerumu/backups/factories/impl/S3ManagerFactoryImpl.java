package ru.rerumu.backups.factories.impl;

import ru.rerumu.backups.factories.S3ManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.S3Manager;
import ru.rerumu.backups.services.impl.ListManager;
import ru.rerumu.backups.services.impl.MultipartUploadManager;
import ru.rerumu.backups.services.impl.OnepartDownloadManager;
import ru.rerumu.backups.services.impl.OnepartUploadManager;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Path;

public class S3ManagerFactoryImpl implements S3ManagerFactory {
    private final int maxPartSize;

    public S3ManagerFactoryImpl(int maxPartSize){
        this.maxPartSize = maxPartSize;
    }

    @Override
    public S3Manager getUploadManager(BufferedInputStream bufferedInputStream, long size, S3Storage s3Storage, String key, S3Client s3Client)
            throws IOException {
        if (size > maxPartSize){
            return new MultipartUploadManager(bufferedInputStream, size, s3Storage, key, s3Client, maxPartSize);
        } else {
            return new OnepartUploadManager(bufferedInputStream, s3Storage, key, s3Client);
        }
    }

    @Override
    public S3Manager getDownloadManager(S3Storage s3Storage,String key, S3Client s3Client, Path path, long size) {
        if (size > maxPartSize){
            throw new IllegalArgumentException();
        } else {
            return new OnepartDownloadManager(s3Storage,key,s3Client,path);
        }
    }

    @Override
    public S3Manager getDownloadManager(S3Storage s3Storage,String key, S3Client s3Client, Path path) {
        return new OnepartDownloadManager(s3Storage,key,s3Client,path);
    }

    @Override
    public ListManager getListManager(S3Storage s3Storage, String key, S3Client s3Client) {
        return new ListManager(s3Storage,key,s3Client);
    }
}
