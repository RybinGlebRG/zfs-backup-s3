package ru.rerumu.zfs_backup_s3.s3.services.impl.requests;

import org.checkerframework.checker.nullness.qual.NonNull;
import ru.rerumu.zfs_backup_s3.s3.factories.S3ClientFactory;
import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.UploadPartResult;
import ru.rerumu.zfs_backup_s3.utils.ByteArray;
import ru.rerumu.zfs_backup_s3.utils.ByteArrayList;
import ru.rerumu.zfs_backup_s3.utils.ImmutableList;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.callables.impl.CallableSupplier;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

@ThreadSafe
public final class CallableSupplierFactory {

    private final S3ClientFactory s3ClientFactory;
    private final S3Storage s3Storage;

    public CallableSupplierFactory(
            @NonNull S3ClientFactory s3ClientFactory,
            @NonNull S3Storage s3Storage) {
        Objects.requireNonNull(s3ClientFactory);
        Objects.requireNonNull(s3Storage);
        this.s3ClientFactory = s3ClientFactory;
        this.s3Storage = s3Storage;
    }

    public CallableSupplier<String> getCreateMultipartUploadSupplier(
            String key){
        return new CallableSupplier<>(() -> new CreateMultipartUploadCallable(
                s3Storage.getBucketName(),
                key,
                s3Storage.getStorageClass(),
                s3ClientFactory.getS3Client(s3Storage)
        ));
    }

    public CallableSupplier<UploadPartResult> getUploadPartSupplier(
            String key,
            String uploadId,
            Integer partNumber,
            ByteArray data
    ){
        return new CallableSupplier<>(() ->new UploadPartCallable(
                s3Storage.getBucketName(),
                key,
                s3ClientFactory.getS3Client(s3Storage),
                uploadId,
                partNumber,
                data
        ));
    }

    public CallableSupplier<AbortMultipartUploadResponse> getAbortMultipartUploadSupplier(String key, String uploadId){
        return new CallableSupplier<>(()-> new AbortMultipartUploadCallable(
                s3Storage.getBucketName(),
                key,
                s3ClientFactory.getS3Client(s3Storage),
                uploadId
        ));
    }

    public CallableSupplier<Void> getCompleteMultipartUploadSupplier(
            ImmutableList<CompletedPart> completedPartList, String key, String uploadId, ByteArrayList md5List
    ){
        return new CallableSupplier<>(()->new CompleteMultipartUploadCallable(
                completedPartList,
                s3Storage.getBucketName(),
                key,
                uploadId,
                s3ClientFactory.getS3Client(s3Storage),
                md5List
        ));
    }

    public CallableSupplier<ListObjectsResponse> getListObjectSupplier(String key){
        return new CallableSupplier<>(()-> new ListObjectCallable(
                s3Storage.getBucketName(),
                key,
                s3ClientFactory.getS3Client(s3Storage),
                null
        ));
    }

    public CallableSupplier<ListObjectsResponse> getListObjectSupplier(String key, String nextMarker){
        return new CallableSupplier<>(()->new ListObjectCallable(
                s3Storage.getBucketName(),
                key,
                s3ClientFactory.getS3Client(s3Storage),
                nextMarker
        ));
    }

    public CallableSupplier<Void> getPutObjectSupplier(Path sourcePath, String targetKey){
        return new CallableSupplier<>(()-> new PutObjectCallable(
                s3Storage.getBucketName(),
                targetKey,
                s3Storage.getStorageClass(),
                s3ClientFactory.getS3Client(s3Storage),
                sourcePath
        ));
    }

    public CallableSupplier<byte[]> getGetObjectRangedSupplier(
            String key, Long startInclusive, Long endExclusive, Path targetPath
    ){
        return new CallableSupplier<>(()-> new GetObjectRangedCallable(
                key,
                s3Storage.getBucketName(),
                startInclusive,
                endExclusive,
                s3ClientFactory.getS3Client(s3Storage),
                targetPath
        ));
    }
}
