package ru.rerumu.zfs_backup_s3.s3.services.impl.requests;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class AbortMultipartUploadCallable implements Callable<AbortMultipartUploadResponse> {
    private final String bucketName;
    private final String key;
    private final S3Client s3Client;
    private final String uploadId;

    public AbortMultipartUploadCallable(String bucketName, String key, S3Client s3Client, String uploadId) {
        this.bucketName = bucketName;
        this.key = key;
        this.s3Client = s3Client;
        this.uploadId = uploadId;
    }

    @Override
    public AbortMultipartUploadResponse call() throws Exception {
        AbortMultipartUploadRequest abortMultipartUploadRequest = AbortMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .build();
        AbortMultipartUploadResponse response = s3Client.abortMultipartUpload(abortMultipartUploadRequest);

        return response;
    }
}
