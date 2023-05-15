package ru.rerumu.s3.services.impl;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.factories.S3ClientFactory;
import ru.rerumu.s3.services.impl.requests.*;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.s3.services.S3RequestService;
import ru.rerumu.s3.services.impl.requests.models.ListObject;
import ru.rerumu.s3.services.impl.requests.models.UploadPartResult;
import ru.rerumu.utils.callables.CallableExecutor;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public UploadPartResult uploadPart(String key, String uploadId, Integer partNumber, byte[] data) {
        UploadPartResult partResult = callableExecutor.callWithRetry(()->
                new UploadPartCallable(
                        s3Storage.getBucketName(),
                        key,
                        s3ClientFactory.getS3Client(s3Storage),
                        uploadId,
                        partNumber,
                        data
                )
        );
        return partResult;
    }

    @Override
    public String createMultipartUpload(String key) {
        String uploadId = callableExecutor.callWithRetry(() ->
                new CreateMultipartUploadCallable(
                        s3Storage.getBucketName(),
                        key,
                        s3Storage.getStorageClass(),
                        s3ClientFactory.getS3Client(s3Storage)
                )
        );
        return uploadId;
    }

    @Override
    public void abortMultipartUpload(String key, String uploadId) {
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
    }

    @Override
    public void completeMultipartUpload(List<CompletedPart> completedPartList, String key, String uploadId, List<byte[]> md5List) {
        callableExecutor.callWithRetry(() ->
                new CompleteMultipartUploadCallable(
                        completedPartList,
                        s3Storage.getBucketName(),
                        key,
                        uploadId,
                        s3ClientFactory.getS3Client(s3Storage),
                        md5List
                )
        );
    }

    @Override
    public List<ListObject> listObjects(String key) {
        ListObjectsResponse response = callableExecutor.callWithRetry(()->
                new ListObjectCallable(
                        s3Storage.getBucketName(),
                        key,
                        s3ClientFactory.getS3Client(s3Storage),
                        null
                )
        );

        List<ListObject> result = response.contents().stream()
                .map(item->new ListObject(item.key(), item.eTag(), item.size()))
                .collect(Collectors.toCollection(ArrayList::new));

        while (response.isTruncated()){
            String nextMarker = response.nextMarker();
            Objects.requireNonNull(nextMarker);
            logger.debug(String.format("Next marker = '%s'",nextMarker));

            response = callableExecutor.callWithRetry(()->
                    new ListObjectCallable(
                            s3Storage.getBucketName(),
                            key,
                            s3ClientFactory.getS3Client(s3Storage),
                            nextMarker
                    )
            );

            response.contents().stream()
                    .map(item->new ListObject(item.key(), item.eTag(), item.size()))
                    .forEachOrdered(result::add);
        }

        return result;
    }

    @Override
    public ListObject getMetadata(String key) {
        List<ListObject> objects = listObjects(key);
        if(objects.size() != 1){
            throw new AssertionError();
        } else {
            return objects.get(0);
        }
    }


    @Override
    public void putObject(Path sourcePath, String targetKey) {
        callableExecutor.callWithRetry(()-> new PutObjectCallable(
                s3Storage.getBucketName(),
                targetKey,
                s3Storage.getStorageClass(),
                s3ClientFactory.getS3Client(s3Storage),
                sourcePath
        ));
    }

    @Override
    public byte[] getObjectRange(String key, Long start, Long finish, Path targetPath) {
        byte[] md5 = callableExecutor.callWithRetry(()->new GetObjectRangedCallable(
                key,
                s3Storage.getBucketName(),
                start,
                finish,
                s3ClientFactory.getS3Client(s3Storage),
                targetPath
        ));
        return md5;
    }
}
