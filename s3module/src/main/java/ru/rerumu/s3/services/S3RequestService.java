package ru.rerumu.s3.services;

import ru.rerumu.s3.services.impl.requests.models.ListObject;
import ru.rerumu.s3.services.impl.requests.models.UploadPartResult;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.file.Path;
import java.util.List;

public interface S3RequestService {

    UploadPartResult uploadPart(
            String key, String uploadId, Integer partNumber, byte[] data
    );

//    CreateMultipartUploadResponse createMultipartUpload(
//            String key
//    );

    String createMultipartUpload(
            String key
    );

    void abortMultipartUpload(
            String key, String uploadId
    );

    void completeMultipartUpload(
            List<CompletedPart> completedPartList, String key, String uploadId,List<byte[]> md5List
    );

    List<ListObject> listObjects(
            String key
    );

    ListObject getMetadata(String key);


    void putObject(Path sourcePath, String targetKey);

    byte[] getObjectRange(String key,Long startInclusive, Long endExclusive, Path targetPath);
}
