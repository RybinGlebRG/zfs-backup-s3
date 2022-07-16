package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.models.S3Storage;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

public class OnepartDownloadManager extends AbstractS3Manager {
    private final Logger logger = LoggerFactory.getLogger(OnepartDownloadManager.class);
    private final S3Storage s3Storage;
    private final String key;
    private final S3Client s3Client;
    private final Path path;

    public OnepartDownloadManager(S3Storage s3Storage, String key, S3Client s3Client, Path path){
        this.path = path;
        this.s3Storage =s3Storage;
        this.key = key;
        this.s3Client = s3Client;
    }

    @Override
    public void run() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Storage.getBucketName())
                .key(key)
                .build();
        GetObjectResponse getObjectResponse = s3Client.getObject(
                getObjectRequest,
                path);
        String eTag = getObjectResponse.eTag();
        logger.info(String.format("ETag='%s'", eTag));
        String md5 = getMD5Hex(path);
        if (!(eTag.equals('"' + md5 + '"'))) {
            throw new IncorrectHashException();
        }
    }
}
