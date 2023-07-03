package ru.rerumu.zfs_backup_s3.s3.services.impl.requests;

import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.rerumu.zfs_backup_s3.s3.exceptions.IncorrectHashException;
import ru.rerumu.zfs_backup_s3.utils.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@ThreadSafe
public final class CompleteMultipartUploadCallable extends CallableOnlyOnce<Void> {

    private final ImmutableList<CompletedPart> completedPartList;
    private final String bucketName;
    private final String key;
    private final String uploadId;
    private final S3Client s3Client;

    private final ByteArrayList md5List;
    public CompleteMultipartUploadCallable(
            @NonNull ImmutableList<CompletedPart> completedPartList,
            @NonNull String bucketName,
            @NonNull String key,
            @NonNull String uploadId,
            @NonNull S3Client s3Client,
            @NonNull ByteArrayList md5List
    ) {
        Objects.requireNonNull(completedPartList);
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(key);
        Objects.requireNonNull(uploadId);
        Objects.requireNonNull(s3Client);
        Objects.requireNonNull(md5List);
        this.completedPartList = completedPartList;
        this.bucketName = bucketName;
        this.key = key;
        this.uploadId = uploadId;
        this.s3Client = s3Client;
        this.md5List = md5List;
    }

    @Override
    protected Void callOnce() throws Exception {
        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(completedPartList.list())
                .build();

        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                CompleteMultipartUploadRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .uploadId(uploadId)
                        .multipartUpload(completedMultipartUpload)
                        .build();

        CompleteMultipartUploadResponse completeMultipartUploadResponse =
                s3Client.completeMultipartUpload(completeMultipartUploadRequest);

        String eTag = completeMultipartUploadResponse.eTag();

        byte[] concatenatedMd5 = md5List.list().stream()
                .map(ByteArray::array)
                .reduce(new byte[0], ArrayUtils::addAll,ArrayUtils::addAll);
        String md5 = MD5.getMD5Hex(new ByteArray(concatenatedMd5)) + "-" + md5List.list().size();

        if (!eTag.equals('"' + md5 + '"')) {
            throw new IncorrectHashException(String.format("Got '%s', but expected '%s'", eTag, '"' + md5 + '"'));
        }

        return null;
    }
}
