package ru.rerumu.s3.impl.helper;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

public class UploadPartCallable implements Callable<Map<String,String>> {
    private final String bucketName;
    private final String key;
    private final S3Client s3Client;
    private final String uploadId;
    private final Integer partNumber;
    private final byte[] data;

    public UploadPartCallable(String bucketName, String key, S3Client s3Client, String uploadId, Integer partNumber, byte[] data) {
        this.bucketName = bucketName;
        this.key = key;
        this.s3Client = s3Client;
        this.uploadId = uploadId;
        this.partNumber = partNumber;
        this.data = data;
    }

    @Override
    public Map<String, String> call() throws Exception {
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber).build();

        String eTag = s3Client.uploadPart(
                uploadPartRequest, RequestBody.fromBytes(data)
        ).eTag();
        Objects.requireNonNull(eTag);

        Map<String,String> res = new HashMap<>();
        res.put("eTag",eTag);
        return res;
    }
}
