package ru.rerumu.s3.services.impl.requests;

import ru.rerumu.s3.factories.S3ClientFactory;
import ru.rerumu.s3.models.S3Storage;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class CallableSupplierFactory {

    private final S3ClientFactory s3ClientFactory;
    private final S3Storage s3Storage;

    public CallableSupplierFactory(S3ClientFactory s3ClientFactory, S3Storage s3Storage) {
        this.s3ClientFactory = s3ClientFactory;
        this.s3Storage = s3Storage;
    }

    public Supplier<Callable<String>> getCreateMultipartUploadSupplier(
            String key){
        return () -> new CreateMultipartUploadCallable(
                s3Storage.getBucketName(),
                key,
                s3Storage.getStorageClass(),
                s3ClientFactory.getS3Client(s3Storage)
        );
    }
}
