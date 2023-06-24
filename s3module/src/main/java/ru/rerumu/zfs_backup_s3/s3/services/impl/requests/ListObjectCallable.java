package ru.rerumu.zfs_backup_s3.s3.services.impl.requests;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import ru.rerumu.zfs_backup_s3.utils.CallableOnlyOnce;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

@ThreadSafe
public final class ListObjectCallable extends CallableOnlyOnce<ListObjectsResponse> {
    private final String bucketName;
    private final String key;
    private final S3Client s3Client;
    private final String marker;

    public ListObjectCallable(
            @NonNull String bucketName,
            @NonNull String key,
            @NonNull S3Client s3Client,
            @Nullable String marker) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(key);
        Objects.requireNonNull(s3Client);
        this.bucketName = bucketName;
        this.key = key;
        this.s3Client = s3Client;
        this.marker = marker;
    }

    @Override
    protected ListObjectsResponse callOnce() throws Exception {
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
