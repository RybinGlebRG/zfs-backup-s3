package ru.rerumu.s3.services.impl.requests;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;

import java.util.Objects;
import java.util.concurrent.Callable;

public class CreateMultipartUploadCallable implements Callable<String> {
    private final String bucketName;
    private final String key;
    private final String storageClass;

    private final S3Client s3Client;

    public CreateMultipartUploadCallable(String bucketName, String key, String storageClass, S3Client s3Client) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(key);
        Objects.requireNonNull(storageClass);
        Objects.requireNonNull(s3Client);

        this.bucketName = bucketName;
        this.key = key;
        this.storageClass = storageClass;
        this.s3Client = s3Client;
    }

    @Override
    public String call() throws Exception {
        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .storageClass(storageClass)
                .build();
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createMultipartUploadRequest);

        String uploadId = response.uploadId();
        Objects.requireNonNull(uploadId);

        return uploadId;
    }
}
