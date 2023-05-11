package ru.rerumu.s3.services.impl.helper;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ListObjectCallable implements Callable<ListObjectsResponse> {
    private final String bucketName;
    private final String key;
    private final S3Client s3Client;

    public ListObjectCallable(String bucketName, String key, S3Client s3Client) {
        this.bucketName = bucketName;
        this.key = key;
        this.s3Client = s3Client;
    }

    @Override
    public ListObjectsResponse call() throws Exception {
        ListObjectsRequest listObjects = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(key)
                .build();
        ListObjectsResponse res = s3Client.listObjects(listObjects);
        return res;
    }
}
