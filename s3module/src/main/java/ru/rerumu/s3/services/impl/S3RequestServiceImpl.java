package ru.rerumu.s3.services.impl;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.factories.S3ClientFactory;
import ru.rerumu.s3.services.impl.helper.PutObjectCallable;
import ru.rerumu.s3.services.impl.helper.ListObjectCallable;
import ru.rerumu.s3.services.impl.helper.CompleteMultipartUploadCallable;
import ru.rerumu.s3.services.impl.helper.AbortMultipartUploadCallable;
import ru.rerumu.s3.services.impl.helper.CreateMultipartUploadCallable;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.s3.services.S3RequestService;
import ru.rerumu.s3.services.impl.helper.UploadPartCallable;
import ru.rerumu.utils.callables.CallableExecutor;
import software.amazon.awssdk.services.s3.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class S3RequestServiceImpl implements S3RequestService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CallableExecutor callableExecutor;
    private final S3ClientFactory s3ClientFactory;
    private final S3Storage s3Storage;

    public S3RequestServiceImpl(CallableExecutor callableExecutor, S3ClientFactory s3ClientFactory, S3Storage s3Storage) {
        this.callableExecutor = callableExecutor;
        this.s3ClientFactory = s3ClientFactory;
        this.s3Storage = s3Storage;
    }

    // TODO: Max part number?
    @Override
    public UploadPartResponse uploadPart(String key, String uploadId, Integer partNumber, byte[] data) {
        UploadPartResponse response = callableExecutor.callWithRetry(()->
                new UploadPartCallable(
                        s3Storage.getBucketName(),
                        key,
                        s3ClientFactory.getS3Client(s3Storage),
                        uploadId,
                        partNumber,
                        data
                )
        );
        return response;
    }

    @Override
    public CreateMultipartUploadResponse createMultipartUpload(String key) {
        CreateMultipartUploadResponse response = callableExecutor.callWithRetry(() ->
                new CreateMultipartUploadCallable(
                        s3Storage.getBucketName(),
                        key,
                        s3Storage.getStorageClass(),
                        s3ClientFactory.getS3Client(s3Storage)
                )
        );
        return response;
    }

    @Override
    public AbortMultipartUploadResponse abortMultipartUpload(@NonNull String key, @NonNull String uploadId) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(uploadId);
        logger.info(String.format("Aborting upload by id '%s'", uploadId));

        AbortMultipartUploadResponse response = callableExecutor.callWithRetry(() ->
                new AbortMultipartUploadCallable(
                        s3Storage.getBucketName(),
                        key,
                        s3ClientFactory.getS3Client(s3Storage),
                        uploadId
                )
        );

        logger.info(String.format("Upload '%s' aborted", uploadId));
        return response;
    }

    @Override
    public CompleteMultipartUploadResponse completeMultipartUpload(List<CompletedPart> completedPartList, String key, String uploadId) {
        CompleteMultipartUploadResponse response = callableExecutor.callWithRetry(() ->
                new CompleteMultipartUploadCallable(
                        completedPartList,
                        s3Storage.getBucketName(),
                        key,
                        uploadId,
                        s3ClientFactory.getS3Client(s3Storage)
                )
        );
        return response;
    }

    @Override
    public ListObjectsResponse listObjects(String key) {
        ListObjectsResponse response = callableExecutor.callWithRetry(()->
                new ListObjectCallable(
                        s3Storage.getBucketName(),
                        key,
                        s3ClientFactory.getS3Client(s3Storage)
                )
        );
        return response;
    }

    @Override
    public PutObjectResponse putObject(String key, byte[] data) {
        PutObjectResponse putObjectResponse = callableExecutor.callWithRetry(() -> new PutObjectCallable(
                s3Storage.getBucketName(),
                key,
                s3Storage.getStorageClass(),
                s3ClientFactory.getS3Client(s3Storage),
                data
        ));
        return putObjectResponse;
    }
}
