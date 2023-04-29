package ru.rerumu.backups.services.s3.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.models.S3Storage;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static ru.rerumu.backups.utils.MD5.getMD5Bytes;
import static ru.rerumu.backups.utils.MD5.getMD5Hex;

public class MultipartUploadCallable implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Path path;
    private final String key;
    private final S3Storage s3Storage;
    private final S3ClientFactory s3ClientFactory;

    private final int maxPartSize;

    public MultipartUploadCallable(Path path, String key, S3Storage s3Storage, S3ClientFactory s3ClientFactory, int maxPartSize) {
        this.path = path;
        this.key = key;
        this.s3Storage = s3Storage;
        this.s3ClientFactory = s3ClientFactory;
        this.maxPartSize = maxPartSize;
    }

    private String start(S3Client s3Client){
        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                .bucket(s3Storage.getBucketName())
                .key(key)
                .storageClass(s3Storage.getStorageClass())
                .build();
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createMultipartUploadRequest);
        String uploadId = response.uploadId();
        logger.info(String.format("uploadId '%s'", uploadId));

        return uploadId;
    }

    private byte[] getNextPart(BufferedInputStream bufferedInputStream) throws IOException, EOFException {
        byte[] tmp = new byte[maxPartSize];
        int len = bufferedInputStream.read(tmp);
        if (len==-1){
            throw new EOFException();
        }
        return Arrays.copyOf(tmp,len);
    }

    private void uploadPart(
            byte[] data,
            Integer partNumber,
            S3Client s3Client,
            String uploadId,
            List<byte[]> md5List,
            List<CompletedPart> completedPartList
    ) throws IOException, EOFException, NoSuchAlgorithmException, IncorrectHashException {
        logger.debug("Getting new part");
        logger.debug(String.format("Starting loading part '%d'",partNumber));
        String md5 = getMD5Hex(data);
        // TODO: Max part number?
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(s3Storage.getBucketName())
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber).build();

        String eTag = s3Client.uploadPart(
                uploadPartRequest, RequestBody.fromBytes(data)
        ).eTag();
        logger.info(String.format("ETag='%s'", eTag));
        if (!(eTag.equals('"' + md5 + '"'))) {
            throw new IncorrectHashException(String.format("Got '%s', but expected '%s'",eTag, '"' + md5 + '"'));
        }
        md5List.add(getMD5Bytes(data));

        completedPartList.add(
                CompletedPart.builder().partNumber(partNumber).eTag(eTag).build()
        );
        logger.debug(String.format("Uploaded part '%d'",partNumber));
    }

    private void finish(
            List<CompletedPart> completedPartList,
            String uploadId,
            S3Client s3Client,
            List<byte[]> md5List,
            Integer partNumber
    ) throws NoSuchAlgorithmException, IOException, IncorrectHashException {
        CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                .parts(completedPartList)
                .build();

        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                CompleteMultipartUploadRequest.builder()
                        .bucket(s3Storage.getBucketName())
                        .key(key)
                        .uploadId(uploadId)
                        .multipartUpload(completedMultipartUpload)
                        .build();

        CompleteMultipartUploadResponse completeMultipartUploadResponse =
                s3Client.completeMultipartUpload(completeMultipartUploadRequest);

        byte[] concatenatedMd5 = new byte[0];
        for (byte[] bytes: md5List){
            concatenatedMd5 = ArrayUtils.addAll(concatenatedMd5,bytes);
        }
        String md5 = getMD5Hex(concatenatedMd5)+"-"+partNumber;
        String eTag = completeMultipartUploadResponse.eTag();
        if (!eTag.equals('"' + md5 + '"')){
            throw new IncorrectHashException(String.format("Got '%s', but expected '%s'",eTag, '"' + md5 + '"'));
        }
    }

    private void abort(String uploadId, S3Client s3Client){
        logger.info(String.format("Aborting upload by id '%s'",uploadId));
        AbortMultipartUploadRequest abortMultipartUploadRequest = AbortMultipartUploadRequest.builder()
                .bucket(s3Storage.getBucketName())
                .key(key)
                .uploadId(uploadId)
                .build();
        s3Client.abortMultipartUpload(abortMultipartUploadRequest);
        logger.info(String.format("Upload '%s' aborted",uploadId));
    }

    @Override
    public Void call() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path))) {
            S3Client s3Client = s3ClientFactory.getS3Client(s3Storage);
            String uploadId = null;
            int partNumber = 0;
            List<byte[]> md5List = new ArrayList<>();
            List<CompletedPart> completedPartList = new ArrayList<>();

            try {


                uploadId = start(s3Client);

                while (true) {
                    try {
                        byte[] nextPart = getNextPart(bufferedInputStream);
                        partNumber++;
                        uploadPart(nextPart, partNumber, s3Client,uploadId,md5List,completedPartList);
                    } catch (EOFException e) {
                        break;
                    }
                }

                finish(
                        completedPartList,
                        uploadId,
                        s3Client,
                        md5List,
                        partNumber
                );
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                if (uploadId != null) {
                    abort(uploadId, s3Client);
                }
                throw e;
            }
        }
        return null;
    }
}
