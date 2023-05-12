package ru.rerumu.s3.services.impl.requests;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class CompleteMultipartUploadCallable implements Callable<CompleteMultipartUploadResponse> {

    private final List<CompletedPart> completedPartList;
    private final String bucketName;
    private final String key;
    private final String uploadId;
    private final S3Client s3Client;

    public CompleteMultipartUploadCallable(List<CompletedPart> completedPartList, String bucketName, String key, String uploadId, S3Client s3Client) {
        this.completedPartList = completedPartList;
        this.bucketName = bucketName;
        this.key = key;
        this.uploadId = uploadId;
        this.s3Client = s3Client;
    }

    @Override
    public CompleteMultipartUploadResponse call() throws Exception {
        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(completedPartList)
                .build();

        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                CompleteMultipartUploadRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .uploadId(uploadId)
                        .multipartUpload(completedMultipartUpload)
                        .build();

        CompleteMultipartUploadResponse completeMultipartUploadResponse =
                s3Client.completeMultipartUpload(completeMultipartUploadRequest);

        return completeMultipartUploadResponse;
    }
}
