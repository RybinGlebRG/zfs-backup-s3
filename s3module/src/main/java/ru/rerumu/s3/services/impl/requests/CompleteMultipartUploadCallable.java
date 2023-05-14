package ru.rerumu.s3.services.impl.requests;

import org.apache.commons.lang3.ArrayUtils;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.utils.MD5;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;

import java.util.List;
import java.util.concurrent.Callable;

public class CompleteMultipartUploadCallable implements Callable<Void> {

    private final List<CompletedPart> completedPartList;
    private final String bucketName;
    private final String key;
    private final String uploadId;
    private final S3Client s3Client;

    private final List<byte[]> md5List;
    public CompleteMultipartUploadCallable(
            List<CompletedPart> completedPartList,
            String bucketName,
            String key,
            String uploadId,
            S3Client s3Client,
            List<byte[]> md5List
    ) {
        this.completedPartList = completedPartList;
        this.bucketName = bucketName;
        this.key = key;
        this.uploadId = uploadId;
        this.s3Client = s3Client;
        this.md5List = md5List;
    }

    @Override
    public Void call() throws Exception {
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

        String eTag = completeMultipartUploadResponse.eTag();

        byte[] concatenatedMd5 = md5List.stream()
                .reduce(new byte[0], ArrayUtils::addAll,ArrayUtils::addAll);
        String md5 = MD5.getMD5Hex(concatenatedMd5) + "-" + md5List.size();

        if (!eTag.equals('"' + md5 + '"')) {
            throw new IncorrectHashException(String.format("Got '%s', but expected '%s'", eTag, '"' + md5 + '"'));
        }

        return null;
    }
}
