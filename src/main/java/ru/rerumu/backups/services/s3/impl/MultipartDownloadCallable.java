package ru.rerumu.backups.services.s3.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.utils.MD5;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static ru.rerumu.backups.utils.MD5.getMD5Bytes;
import static ru.rerumu.backups.utils.MD5.getMD5Hex;

public class MultipartDownloadCallable implements Callable<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Path path;
    private final String key;
    private final S3Storage s3Storage;
    private final S3ClientFactory s3ClientFactory;

    private final int maxPartSize;

    public MultipartDownloadCallable(Path path, String key, S3Storage s3Storage, S3ClientFactory s3ClientFactory, int maxPartSize) {
        this.path = path;
        this.key = key;
        this.s3Storage = s3Storage;
        this.s3ClientFactory = s3ClientFactory;
        this.maxPartSize = maxPartSize;
    }

    private Long getSize(S3Client s3Client){
        ListObjectsRequest listObjects = ListObjectsRequest.builder()
                .bucket(s3Storage.getBucketName())
                .prefix(key)
                .build();

        ListObjectsResponse res = s3Client.listObjects(listObjects);
        List<S3Object> s3Objects = res.contents();
        logger.info(String.format("Found on S3:\n'%s'", s3Objects));

        if (s3Objects.size() > 1) {
            throw new IllegalArgumentException("Cannot have two files with the same key");
        }

        return s3Objects.get(0).size();
    }

    public String getETag(S3Client s3Client){
        ListObjectsRequest listObjects = ListObjectsRequest.builder()
                .bucket(s3Storage.getBucketName())
                .prefix(key)
                .build();

        ListObjectsResponse res = s3Client.listObjects(listObjects);
        List<S3Object> s3Objects = res.contents();
        logger.info(String.format("Found on S3:\n'%s'", s3Objects));

        if (s3Objects.size() > 1) {
            throw new IllegalArgumentException();
        }

        return s3Objects.get(0).eTag();
    }

    private void downloadRange(long start, long finish, S3Client s3Client, List<byte[]> md5List) throws NoSuchAlgorithmException, IOException, IncorrectHashException {
        logger.debug(String.format("Loading range from '%d' to '%d'", start, finish));
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .range(String.format("bytes=%d-%d", start, finish))
                .key(key)
                .bucket(s3Storage.getBucketName())
                .build();
        ResponseBytes<GetObjectResponse> response = s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes());
        byte[] responseBytes = response.asByteArray();

        // TODO: ???
        GetObjectResponse getObjectResponse = response.response();
        md5List.add(MD5.getMD5Bytes(responseBytes));

        Files.write(
                path,
                responseBytes,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
        logger.debug("Finished loading range");
    }

    private void finish(S3Client s3Client) throws NoSuchAlgorithmException, IOException, IncorrectHashException {
        String md5 = MD5.getMD5Hex(path);
        String storedMd5Hex = StringUtils.strip(getETag(s3Client),"\"");

        logger.info(String.format("Calculated md5='%s'", md5));
        logger.info(String.format("Stored md5='%s'", storedMd5Hex));
        if (!storedMd5Hex.equals(md5)) {
            throw new IncorrectHashException();
        }
    }


    @Override
    public Void call() throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        S3Client s3Client = s3ClientFactory.getS3Client(s3Storage);
        final long MAX_END = getSize(s3Client) - 1;
        long start = 0;
        long end = Math.min((start + maxPartSize), MAX_END);
        List<byte[]> md5List = new ArrayList<>();

        while (true) {
            downloadRange(start, end, s3Client, md5List);

            if (end == MAX_END) {
                break;
            }

            start = end + 1;
            end = Math.min(start + maxPartSize, MAX_END);
        }

        finish(s3Client);

        return null;
    }
}
