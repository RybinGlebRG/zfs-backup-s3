package ru.rerumu.backups.services.s3.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.models.S3Storage;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Deprecated
public class MultipartUploadManager extends AbstractS3Manager {
    private final Logger logger = LoggerFactory.getLogger(MultipartUploadManager.class);
    private final List<CompletedPart> completedPartList = new ArrayList<>();
    private final List<byte[]> md5List = new ArrayList<>();

    private final S3Storage s3Storage;
    private final String key;
    private final S3Client s3Client;
    private final BufferedInputStream bufferedInputStream;
    private final int maxPartSize;

    private int partNumber=0;

    private String uploadId;


    public MultipartUploadManager(
            BufferedInputStream bufferedInputStream,
            long fileSize,
            S3Storage s3Storage,
            String key,
            S3Client s3Client,
            int maxPartSize
    ){
        this.bufferedInputStream = bufferedInputStream;
        this.s3Storage =s3Storage;
        this.key = key;
        this.s3Client = s3Client;
        this.maxPartSize = maxPartSize;
    }

    private byte[] getNextPart() throws IOException, EOFException {
        byte[] tmp = new byte[maxPartSize];
        int len = bufferedInputStream.read(tmp);
        if (len==-1){
            throw new EOFException();
        }
        return Arrays.copyOf(tmp,len);
    }

    private void abort(){
        logger.info(String.format("Aborting upload by id '%s'",uploadId));
        AbortMultipartUploadRequest abortMultipartUploadRequest = AbortMultipartUploadRequest.builder()
                .bucket(s3Storage.getBucketName())
                .key(key)
                .uploadId(uploadId)
                .build();
        s3Client.abortMultipartUpload(abortMultipartUploadRequest);
        logger.info(String.format("Upload '%s' aborted",uploadId));
    }

    public void run() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        try {
            start();

            while (true) {
                try {
                    uploadPart();
                } catch (EOFException e) {
                    break;
                }
            }

            finish();
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            if (uploadId!=null){
                abort();
            }
            throw  e;
        }
    }

    public void start(){
        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                .bucket(s3Storage.getBucketName())
                .key(key)
                .storageClass(s3Storage.getStorageClass())
                .build();
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(createMultipartUploadRequest);
        uploadId = response.uploadId();
        logger.info(String.format("uploadId '%s'", uploadId));
    }

    private void uploadPart() throws IOException, EOFException, NoSuchAlgorithmException, IncorrectHashException {
        logger.debug("Getting new part");
        byte[] nextPart = getNextPart();
        partNumber++;
        logger.debug(String.format("Starting loading part '%d'",partNumber));
        String md5 = getMD5Hex(nextPart);
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(s3Storage.getBucketName())
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber).build();

        String eTag = s3Client.uploadPart(
                uploadPartRequest, RequestBody.fromBytes(nextPart)
        ).eTag();
        logger.info(String.format("ETag='%s'", eTag));
        if (!(eTag.equals('"' + md5 + '"'))) {
            throw new IncorrectHashException();
        }
        md5List.add(getMD5Bytes(nextPart));

        completedPartList.add(
                CompletedPart.builder().partNumber(partNumber).eTag(eTag).build()
        );
        logger.debug(String.format("Uploaded part '%d'",partNumber));
    }

    private void finish() throws NoSuchAlgorithmException, IOException, IncorrectHashException {
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
            throw new IncorrectHashException();
        }
    }

}
