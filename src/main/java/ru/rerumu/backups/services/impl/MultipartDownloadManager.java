package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.utils.MD5;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;

public class MultipartDownloadManager extends AbstractS3Manager {

    private final Logger logger = LoggerFactory.getLogger(MultipartDownloadManager.class);
    private final S3Storage s3Storage;
    private final String key;
    private final S3Client s3Client;
    private final Path path;
    private final long size;
    private final String storedMd5Hex;
    private final int maxPartSize;

    public MultipartDownloadManager(
            S3Storage s3Storage,
            String key,
            S3Client s3Client,
            Path path,
            int maxPartSize
    ){
        this.s3Storage = s3Storage;
        this.key = key;
        this.s3Client = s3Client;
        this.path = path;
        SizeLoader sizeLoader = new SizeLoader(s3Storage,key,s3Client);
        this.size = sizeLoader.getSize();
        ETAGLoader etagLoader = new ETAGLoader(s3Storage,key,s3Client);
        this.storedMd5Hex = etagLoader.getETag();
        this.maxPartSize = maxPartSize;
    }


    private void downloadRange(long start, long finish) throws NoSuchAlgorithmException, IOException, IncorrectHashException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .range(String.format("bytes=%d-%d", start, finish))
                .key(key)
                .bucket(s3Storage.getBucketName())
                .build();
        ResponseBytes<GetObjectResponse> response = s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes());
        byte[] responseBytes = response.asByteArray();

        GetObjectResponse getObjectResponse = response.response();
        String eTag = getObjectResponse.eTag();
        String md5 = MD5.getMD5Hex(responseBytes);

        if (!(eTag.equals('"' + md5 + '"'))) {
            throw new IncorrectHashException();
        }

        Files.write(
                path,
                responseBytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
    }

    private void finish() throws NoSuchAlgorithmException, IOException, IncorrectHashException {
        String gotMd5 = MD5.getMD5Hex(path);
        if (!gotMd5.equals(storedMd5Hex)) {
            throw new IncorrectHashException();
        }
    }

    @Override
    public void run() throws NoSuchAlgorithmException, IOException, IncorrectHashException {
        final long MAX_END = size-1;

        long start = 0;
        long end = Math.min((start + maxPartSize), MAX_END);

        while (true) {
            downloadRange(start, end);

            if (end==MAX_END){
                break;
            }

            start=end+1;
            end = Math.min(start+maxPartSize,MAX_END);
        }

        finish();
    }
}
