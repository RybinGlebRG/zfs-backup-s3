package ru.rerumu.zfs_backup_s3.s3.services.impl.requests;

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
    private final String marker;

    public ListObjectCallable(String bucketName, String key, S3Client s3Client, String marker) {
        this.bucketName = bucketName;
        this.key = key;
        this.s3Client = s3Client;
        this.marker = marker;
    }

    @Override
    public ListObjectsResponse call() throws Exception {
        ListObjectsRequest.Builder builder = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(key);

        if (marker != null) {
            builder
                    .delimiter("/")
                    .marker(marker);
        }

        ListObjectsResponse res = s3Client.listObjects(builder.build());
        return res;
    }
}
