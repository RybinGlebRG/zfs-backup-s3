package ru.rerumu.backups.services.s3.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.services.s3.factories.S3ClientFactory;
import ru.rerumu.backups.services.s3.models.S3Storage;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

import static ru.rerumu.backups.utils.MD5.getMD5Hex;

public class OnepartUploadCallable implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Path path;
    private final String key;
    private final S3Storage s3Storage;
    private final S3ClientFactory s3ClientFactory;

    public OnepartUploadCallable(Path path, String key, S3Storage s3Storage, S3ClientFactory s3ClientFactory) {
        this.path = path;
        this.key = key;
        this.s3Storage = s3Storage;
        this.s3ClientFactory = s3ClientFactory;
    }

    @Override
    public Void call() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path))) {

            S3Client s3Client = s3ClientFactory.getS3Client(s3Storage);


            byte[] buf = bufferedInputStream.readAllBytes();
            String md5 = getMD5Hex(buf);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Storage.getBucketName())
                    .key(key)
                    .storageClass(s3Storage.getStorageClass())
                    .build();

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(buf));
            String eTag = putObjectResponse.eTag();
            logger.info(String.format("ETag='%s'", eTag));
            if (!(eTag.equals('"' + md5 + '"'))) {
                throw new IncorrectHashException();
            }
        }
        return null;
    }
}
