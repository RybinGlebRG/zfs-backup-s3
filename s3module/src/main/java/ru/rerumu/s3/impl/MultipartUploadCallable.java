package ru.rerumu.s3.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.factories.S3ClientFactory;
import ru.rerumu.s3.impl.helper.factories.HelperCallableFactory;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.utils.MD5;
import ru.rerumu.utils.callables.CallableExecutor;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.Callable;

// TODO: Add multiple attempts?
// TODO: Send with the same part number?
public class MultipartUploadCallable implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Path path;
    private final String key;
    private final S3Storage s3Storage;
    private final S3ClientFactory s3ClientFactory;
    private final int maxPartSize;
    private final CallableExecutor callableExecutor;
    private final HelperCallableFactory helperCallableFactory;

    private final S3Client s3Client;
    private final List<byte[]> md5List = new ArrayList<>();
    private final List<CompletedPart> completedPartList = new ArrayList<>();
    private String uploadId = null;



    public MultipartUploadCallable(Path path, String key, S3Storage s3Storage, S3ClientFactory s3ClientFactory, int maxPartSize, CallableExecutor callableExecutor, HelperCallableFactory helperCallableFactory) {
        this.path = path;
        this.key = key;
        this.s3Storage = s3Storage;
        this.s3ClientFactory = s3ClientFactory;
        this.maxPartSize = maxPartSize;
        this.callableExecutor = callableExecutor;
        this.helperCallableFactory = helperCallableFactory;
        this.s3Client = s3ClientFactory.getS3Client(s3Storage);
    }

    private String start(){
        Map<String,String> response = callableExecutor.callWithRetry(()->
                helperCallableFactory.getCreateMultipartUploadCallable(key)
        );
        String uploadId = response.get("uploadId");
        Objects.requireNonNull(uploadId);
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
            @NonNull byte[] data,
            @NonNull Integer partNumber
    ) throws IOException, EOFException, NoSuchAlgorithmException, IncorrectHashException {
        logger.debug("Getting new part");
        logger.debug(String.format("Starting loading part '%d'",partNumber));

        Objects.requireNonNull(uploadId);
        Objects.requireNonNull(data);
        Objects.requireNonNull(partNumber);

        String md5 = MD5.getMD5Hex(data);

        // TODO: Max part number?
        Map<String,String> response = callableExecutor.callWithRetry(()->
                helperCallableFactory.getUploadPartCallable(
                        key,
                        uploadId,
                        partNumber,
                        data
                )
        );
        String eTag = response.get("eTag");
        Objects.requireNonNull(eTag);

        logger.info(String.format("ETag='%s'", eTag));
        if (!(eTag.equals('"' + md5 + '"'))) {
            throw new IncorrectHashException(String.format("Got '%s', but expected '%s'",eTag, '"' + md5 + '"'));
        }
        md5List.add(MD5.getMD5Bytes(data));

        completedPartList.add(
                CompletedPart.builder().partNumber(partNumber).eTag(eTag).build()
        );
        logger.debug(String.format("Uploaded part '%d'",partNumber));
    }

    private void finish(
            @NonNull S3Client s3Client,
            @NonNull Integer partNumber
    ) throws NoSuchAlgorithmException, IOException, IncorrectHashException {
        Objects.requireNonNull(s3Client);
        Objects.requireNonNull(partNumber);

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
        String md5 = MD5.getMD5Hex(concatenatedMd5)+"-"+partNumber;
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
            int partNumber = 0;

            try {
                uploadId = start();

                while (true) {
                    try {
                        byte[] data = getNextPart(bufferedInputStream);
                        partNumber++;
                        uploadPart(data, partNumber);
                    } catch (EOFException e) {
                        break;
                    }
                }

                finish(s3Client,partNumber);
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
