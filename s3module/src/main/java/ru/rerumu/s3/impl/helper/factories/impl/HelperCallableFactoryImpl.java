package ru.rerumu.s3.impl.helper.factories.impl;

import ru.rerumu.s3.factories.S3ClientFactory;
import ru.rerumu.s3.impl.helper.CreateMultipartUploadCallable;
import ru.rerumu.s3.impl.helper.UploadPartCallable;
import ru.rerumu.s3.impl.helper.factories.HelperCallableFactory;
import ru.rerumu.s3.models.S3Storage;

public class HelperCallableFactoryImpl implements HelperCallableFactory {
    private final S3Storage s3Storage;
    private final S3ClientFactory s3ClientFactory;
    public HelperCallableFactoryImpl(S3Storage s3Storage, S3ClientFactory s3ClientFactory) {
        this.s3Storage = s3Storage;
        this.s3ClientFactory = s3ClientFactory;
    }

    @Override
    public CreateMultipartUploadCallable getCreateMultipartUploadCallable(String key) {
        return new CreateMultipartUploadCallable(
                s3Storage.getBucketName(),
                key,
                s3Storage.getStorageClass(),
                s3ClientFactory.getS3Client(s3Storage)
        );
    }

    @Override
    public UploadPartCallable getUploadPartCallable(String key, String uploadId, Integer partNumber, byte[] data) {
        return new UploadPartCallable(
                s3Storage.getBucketName(),
                key,
                s3ClientFactory.getS3Client(s3Storage),
                uploadId,
                partNumber,
                data
        );
    }
}
