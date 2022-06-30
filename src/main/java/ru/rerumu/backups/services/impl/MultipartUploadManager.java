package ru.rerumu.backups.services.impl;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.UploadManager;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* TODO:
 *   1) Test;
 *   2) Move size to properties
 */

public class MultipartUploadManager implements UploadManager {
    private final static int PART_SIZE=10_485_760;

    private final Logger logger = LoggerFactory.getLogger(MultipartUploadManager.class);
    private final List<CompletedPart> completedPartList = new ArrayList<>();
    private final List<String> md5List = new ArrayList<>();

    private final Path file;
    private final S3Storage s3Storage;
    private final String key;
    private final S3Client s3Client;

    private int partNumber=0;

    private String uploadId;

    public MultipartUploadManager(Path file, S3Storage s3Storage, String key, S3Client s3Client){
        this.file = file;
        this.s3Storage =s3Storage;
        this.key = key;
        this.s3Client = s3Client;
    }

    private String getMD5(byte[] bytes)
            throws NoSuchAlgorithmException,
            IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream)) {

            String md5 = '"' + Hex.encodeHexString(md.digest(bufferedInputStream.readAllBytes())) + '"';
            logger.info(String.format("Part hex MD5: '%s'", md5));
            return md5;

        }
    }

    private byte[] getNextPart() throws IOException, EOFException {
        byte[] tmp = new byte[PART_SIZE];
        int len;
        try(InputStream inputStream = Files.newInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)){
            len = bufferedInputStream.read(tmp);
        }
        if (len==-1){
            throw new EOFException();
        }
        byte[] nextPart = Arrays.copyOf(tmp,len);
        return nextPart;
    }

    private void abort(){
        AbortMultipartUploadRequest abortMultipartUploadRequest = AbortMultipartUploadRequest.builder()
                .bucket(s3Storage.getBucketName())
                .key(key)
                .uploadId(uploadId)
                .build();
        s3Client.abortMultipartUpload(abortMultipartUploadRequest);
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
        partNumber++;
        logger.debug(String.format("Starting loading part '%d'",partNumber));
        byte[] nextPart = getNextPart();
        String md5 = getMD5(nextPart);
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(s3Storage.getBucketName())
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber).build();

        String eTag = s3Client.uploadPart(
                uploadPartRequest, RequestBody.fromBytes(nextPart)
        ).eTag();
        if (!(eTag.equals(md5))) {
            throw new IncorrectHashException();
        }
        md5List.add(md5);

        completedPartList.add(
                CompletedPart.builder().partNumber(partNumber).eTag(eTag).build()
        );
        logger.debug(String.format("Loaded part '%d'",partNumber));
    }

    private void finish(){
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

        s3Client.completeMultipartUpload(completeMultipartUploadRequest);
    }

}
