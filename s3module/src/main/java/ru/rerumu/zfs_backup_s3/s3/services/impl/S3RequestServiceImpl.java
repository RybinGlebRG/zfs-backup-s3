package ru.rerumu.zfs_backup_s3.s3.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.utils.ImmutableMap;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.ListObject;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.UploadPartResult;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.CallableSupplierFactory;
import ru.rerumu.zfs_backup_s3.utils.ByteArray;
import ru.rerumu.zfs_backup_s3.utils.ByteArrayList;
import ru.rerumu.zfs_backup_s3.utils.ImmutableList;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.callables.CallableExecutor;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ThreadSafe
public final class S3RequestServiceImpl implements S3RequestService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CallableExecutor callableExecutor;
    private final CallableSupplierFactory callableSupplierFactory;

    public S3RequestServiceImpl(CallableExecutor callableExecutor, CallableSupplierFactory callableSupplierFactory) {
        this.callableExecutor = callableExecutor;
        this.callableSupplierFactory = callableSupplierFactory;
    }

    // TODO: Max part number?
    @Override
    public UploadPartResult uploadPart(String key, String uploadId, Integer partNumber, ByteArray data) {
        UploadPartResult partResult = callableExecutor.callWithRetry(
                callableSupplierFactory.getUploadPartSupplier(key, uploadId, partNumber, data)
        );
        return partResult;
    }

    @Override
    public String createMultipartUpload(String key, ImmutableMap metadata) {
        String uploadId = callableExecutor.callWithRetry(
                callableSupplierFactory.getCreateMultipartUploadSupplier(key, metadata)
        );
        return uploadId;
    }

    @Override
    public void abortMultipartUpload(String key, String uploadId) {
        logger.info(String.format("Aborting upload by id '%s'", uploadId));

        callableExecutor.callWithRetry(
                callableSupplierFactory.getAbortMultipartUploadSupplier(key, uploadId)
        );

        logger.info(String.format("Upload '%s' aborted", uploadId));
    }

    @Override
    public void completeMultipartUpload(ImmutableList<CompletedPart> completedPartList, String key, String uploadId, ByteArrayList md5List) {
        callableExecutor.callWithRetry(
                callableSupplierFactory.getCompleteMultipartUploadSupplier(completedPartList, key, uploadId, md5List)
        );
    }

    @Override
    public List<ListObject> listObjects(String key) {
        ListObjectsResponse response = callableExecutor.callWithRetry(
                callableSupplierFactory.getListObjectSupplier(key)
        );

        List<ListObject> result = response.contents().stream()
                .map(item -> new ListObject(item.key(), item.eTag(), item.size()))
                .collect(Collectors.toCollection(ArrayList::new));
        logger.debug(String.format("Found on S3: %s", result));

        logger.debug(String.format("Response truncated = '%s'", response.isTruncated()));
        while (response.isTruncated()) {
            String nextMarker = response.nextMarker();
            Objects.requireNonNull(nextMarker);
            logger.debug(String.format("Next marker = '%s'", nextMarker));

            response = callableExecutor.callWithRetry(
                    callableSupplierFactory.getListObjectSupplier(key, nextMarker)
            );

            response.contents().stream()
                    .map(item -> new ListObject(item.key(), item.eTag(), item.size()))
                    .forEachOrdered(result::add);
        }

        return result;
    }

    @Override
    public ListObject getMetadata(String key) {
        List<ListObject> objects = listObjects(key);

        logger.debug(String.format("Checking equality to key='%s'",key));
        objects = objects.stream()
                .peek(item -> logger.debug(String.format("Item key='%s'",item.key())))
                .filter(item -> item.key().equals(key))
                .collect(Collectors.toCollection(ArrayList::new));

        if (objects.size() != 1) {
            throw new AssertionError(String.format("objects size is '%d', not equals '1'", objects.size()));
        } else {
            return objects.get(0);
        }
    }


    @Override
    public void putObject(Path sourcePath, String targetKey) {
        callableExecutor.callWithRetry(
                callableSupplierFactory.getPutObjectSupplier(sourcePath, targetKey)
        );
    }

    @Override
    public byte[] getObjectRange(String key, Long startInclusive, Long endExclusive, Path targetPath) {
        byte[] md5 = callableExecutor.callWithRetry(
                callableSupplierFactory.getGetObjectRangedSupplier(key, startInclusive, endExclusive, targetPath)
        );
        return md5;
    }

    @Override
    public ImmutableMap getObjectMetadata(String key) {
        ImmutableMap metadata = callableExecutor.callWithRetry(
                callableSupplierFactory.getGetObjectMetadataSupplier(key)
        );
        return metadata;
    }
}
