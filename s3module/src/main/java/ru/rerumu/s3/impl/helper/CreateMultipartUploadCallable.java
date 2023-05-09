package ru.rerumu.s3.impl.helper;

import org.checkerframework.checker.nullness.qual.NonNull;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

public class CreateMultipartUploadCallable implements Callable<Map<String,String>> {
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
    public Map<String, String> call() throws Exception {
        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .storageClass(storageClass)
                .build();
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createMultipartUploadRequest);
        Map<String,String> res = new HashMap<>();
        String uploadId = response.uploadId();
        Objects.requireNonNull(uploadId);

        res.put("uploadId", response.uploadId());

        return res;
    }
}
