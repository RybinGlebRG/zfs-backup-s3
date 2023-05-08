package ru.rerumu.s3.impl.helper;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.concurrent.Callable;

// TODO: Check nullable
public final class PutCallable implements Callable<PutObjectResponse> {

    private final String bucketName;
    private final String key;
    private final String storageClass;

    private final S3Client s3Client;

    private final byte[] data;

    public PutCallable(String bucketName, String key, String storageClass, S3Client s3Client, byte[] data) {
        this.bucketName = bucketName;
        this.key = key;
        this.storageClass = storageClass;
        this.s3Client = s3Client;
        // TODO: Defensive copy?
        this.data = data;
    }

    @Override
    public PutObjectResponse call() throws Exception {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .storageClass(storageClass)
                .build();
        PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(data));

        return putObjectResponse;
    }
}
