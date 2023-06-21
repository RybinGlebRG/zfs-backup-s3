package ru.rerumu.zfs_backup_s3.s3.services.impl.requests;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.exceptions.IncorrectHashException;
import ru.rerumu.zfs_backup_s3.utils.MD5;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

// TODO: Check thread safe
// TODO: Check nullable
public final class PutObjectCallable implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String bucketName;
    private final String key;
    private final String storageClass;

    private final S3Client s3Client;

    private final Path sourcePath;

    public PutObjectCallable(String bucketName, String key, String storageClass, S3Client s3Client, Path sourcePath) {
        this.bucketName = bucketName;
        this.key = key;
        this.storageClass = storageClass;
        this.s3Client = s3Client;
        this.sourcePath = sourcePath;
    }

    @Override
    public Void call() throws Exception {

        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(sourcePath))) {

            byte[] buf = bufferedInputStream.readAllBytes();
            String md5 = MD5.getMD5Hex(buf);


            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .storageClass(storageClass)
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
