package ru.rerumu.backups.services.impl;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.UploadManager;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class OnepartUploadManager extends AbstractUploadManager {
    private final Logger logger = LoggerFactory.getLogger(OnepartUploadManager.class);
    private final S3Storage s3Storage;
    private final String key;
    private final S3Client s3Client;
    private final BufferedInputStream bufferedInputStream;

    public OnepartUploadManager(BufferedInputStream bufferedInputStream, S3Storage s3Storage, String key, S3Client s3Client){
        this.bufferedInputStream = bufferedInputStream;
        this.s3Storage =s3Storage;
        this.key = key;
        this.s3Client = s3Client;
    }

    @Override
    public void run() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        byte[] buf = bufferedInputStream.readAllBytes();
        String md5 = getMD5(buf);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Storage.getBucketName())
                .key(key)
                .storageClass(s3Storage.getStorageClass())
                .build();

        PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(buf));
        String eTag = putObjectResponse.eTag();
        logger.info(String.format("ETag='%s'", eTag));
        if (!(eTag.equals(md5))) {
            throw new IncorrectHashException();
        }
    }
}
