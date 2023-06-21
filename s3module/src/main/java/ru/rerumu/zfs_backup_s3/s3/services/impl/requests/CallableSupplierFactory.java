package ru.rerumu.zfs_backup_s3.s3.services.impl.requests;

import ru.rerumu.zfs_backup_s3.s3.factories.S3ClientFactory;
import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.UploadPartResult;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

// TODO: Check thread safe
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

    public Supplier<Callable<UploadPartResult>> getUploadPartSupplier(
            String key,
            String uploadId,
            Integer partNumber,
            byte[] data
    ){
        return () ->new UploadPartCallable(
                s3Storage.getBucketName(),
                key,
                s3ClientFactory.getS3Client(s3Storage),
                uploadId,
                partNumber,
                data
        );
    }

    public Supplier<Callable<AbortMultipartUploadResponse>> getAbortMultipartUploadSupplier(String key, String uploadId){
        return ()-> new AbortMultipartUploadCallable(
                s3Storage.getBucketName(),
                key,
                s3ClientFactory.getS3Client(s3Storage),
                uploadId
        );
    }

    public Supplier<Callable<Void>> getCompleteMultipartUploadSupplier(
            List<CompletedPart> completedPartList, String key, String uploadId, List<byte[]> md5List
    ){
        return ()->new CompleteMultipartUploadCallable(
                completedPartList,
                s3Storage.getBucketName(),
                key,
                uploadId,
                s3ClientFactory.getS3Client(s3Storage),
                md5List
        );
    }

    public Supplier<Callable<ListObjectsResponse>> getListObjectSupplier(String key){
        return ()-> new ListObjectCallable(
                s3Storage.getBucketName(),
                key,
                s3ClientFactory.getS3Client(s3Storage),
                null
        );
    }

    public Supplier<Callable<ListObjectsResponse>> getListObjectSupplier(String key, String nextMarker){
        return ()->new ListObjectCallable(
                s3Storage.getBucketName(),
                key,
                s3ClientFactory.getS3Client(s3Storage),
                nextMarker
        );
    }

    public Supplier<Callable<Void>> getPutObjectSupplier(Path sourcePath, String targetKey){
        return ()-> new PutObjectCallable(
                s3Storage.getBucketName(),
                targetKey,
                s3Storage.getStorageClass(),
                s3ClientFactory.getS3Client(s3Storage),
                sourcePath
        );
    }

    public Supplier<Callable<byte[]> > getGetObjectRangedSupplier(
            String key, Long startInclusive, Long endExclusive, Path targetPath
    ){
        return ()-> new GetObjectRangedCallable(
                key,
                s3Storage.getBucketName(),
                startInclusive,
                endExclusive,
                s3ClientFactory.getS3Client(s3Storage),
                targetPath
        );
    }
}
