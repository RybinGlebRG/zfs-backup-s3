package ru.rerumu.zfs_backup_s3.s3.services.impl.requests;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.utils.ImmutableMap;
import ru.rerumu.zfs_backup_s3.utils.CallableOnlyOnce;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.file.Path;
import java.util.Objects;

@ThreadSafe
public final class GetObjectMetadataCallable extends CallableOnlyOnce<ImmutableMap> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String key;
    private final String bucketName;
    private final S3Client s3Client;

    public GetObjectMetadataCallable(
            @NonNull String key,
            @NonNull String bucketName,
            @NonNull S3Client s3Client
    ) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(s3Client);
        this.key = key;
        this.bucketName = bucketName;
        this.s3Client = s3Client;
    }

    @Override
    protected ImmutableMap callOnce() throws Exception {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .range("bytes=0-0")
                .key(key)
                .bucket(bucketName)
                .build();
        ResponseBytes<GetObjectResponse> response = s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes());
        return new ImmutableMap(response.response().metadata());
    }
}
