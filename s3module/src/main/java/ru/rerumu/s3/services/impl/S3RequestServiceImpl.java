package ru.rerumu.s3.services.impl;

import org.apache.commons.lang3.StringUtils;
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
    private final CallableSupplierFactory callableSupplierFactory;

    public S3RequestServiceImpl(CallableExecutor callableExecutor,  CallableSupplierFactory callableSupplierFactory) {
        this.callableExecutor = callableExecutor;
        this.callableSupplierFactory = callableSupplierFactory;
    }

    // TODO: Max part number?
    @Override
    public UploadPartResult uploadPart(String key, String uploadId, Integer partNumber, byte[] data) {
        UploadPartResult partResult = callableExecutor.callWithRetry(
                callableSupplierFactory.getUploadPartSupplier(key, uploadId, partNumber, data)
        );
        return partResult;
    }

    @Override
    public String createMultipartUpload(String key) {
        String uploadId = callableExecutor.callWithRetry(
                callableSupplierFactory.getCreateMultipartUploadSupplier(key)
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
    public void completeMultipartUpload(List<CompletedPart> completedPartList, String key, String uploadId, List<byte[]> md5List) {
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
                .map(item->new ListObject(item.key(), item.eTag(), item.size()))
                .collect(Collectors.toCollection(ArrayList::new));
        logger.debug(String.format("Found on S3: %s",result));

        while (response.isTruncated()){
            String nextMarker = response.nextMarker();
            Objects.requireNonNull(nextMarker);
            logger.debug(String.format("Next marker = '%s'",nextMarker));

            response = callableExecutor.callWithRetry(
                    callableSupplierFactory.getListObjectSupplier(key,nextMarker)
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
}
