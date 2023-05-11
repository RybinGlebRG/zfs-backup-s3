package ru.rerumu.s3.services;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.Map;

public interface S3RequestService {

    UploadPartResponse uploadPart(
            String key, String uploadId, Integer partNumber, byte[] data
    );

    CreateMultipartUploadResponse createMultipartUpload(
            String key
    );

    AbortMultipartUploadResponse abortMultipartUpload(
            String key, String uploadId
    );

    CompleteMultipartUploadResponse completeMultipartUpload(
            List<CompletedPart> completedPartList, String key, String uploadId
    );

    ListObjectsResponse listObjects(
            String key
    );

    PutObjectResponse putObject(
            String key, byte[] data
    );
}
