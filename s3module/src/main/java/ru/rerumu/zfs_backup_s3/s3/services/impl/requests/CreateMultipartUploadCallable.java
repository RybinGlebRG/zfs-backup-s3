package ru.rerumu.zfs_backup_s3.s3.services.impl.requests;

import org.checkerframework.checker.nullness.qual.NonNull;
import ru.rerumu.zfs_backup_s3.utils.ImmutableMap;
import ru.rerumu.zfs_backup_s3.utils.CallableOnlyOnce;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;

import java.util.Objects;

@ThreadSafe
public final class CreateMultipartUploadCallable extends CallableOnlyOnce<String> {
    private final String bucketName;
    private final String key;
    private final String storageClass;

    private final S3Client s3Client;
    private final ImmutableMap metadata;

    public CreateMultipartUploadCallable(
            @NonNull String bucketName,
            @NonNull String key,
            @NonNull String storageClass,
            @NonNull S3Client s3Client,
            @NonNull ImmutableMap metadata) {
        Objects.requireNonNull(bucketName,"Bucket name cannot be null");
        Objects.requireNonNull(key,"Key cannot be null");
        Objects.requireNonNull(storageClass,"Storage class cannot be null");
        Objects.requireNonNull(s3Client,"S3Client cannot be null");
        Objects.requireNonNull(metadata);

        this.bucketName = bucketName;
        this.key = key;
        this.storageClass = storageClass;
        this.s3Client = s3Client;
        this.metadata = metadata;
    }

    @Override
    protected String callOnce(){
        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .storageClass(storageClass)
                .metadata(metadata.map())
                .build();
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createMultipartUploadRequest);

        String uploadId = response.uploadId();
        Objects.requireNonNull(uploadId);

        return uploadId;
    }
}
