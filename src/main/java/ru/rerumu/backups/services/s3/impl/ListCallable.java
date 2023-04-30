package ru.rerumu.backups.services.s3.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.s3.factories.S3ClientFactory;
import ru.rerumu.backups.services.s3.models.S3Storage;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ListCallable implements Callable<List<String>> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String key;
    private final S3Storage s3Storage;
    private final S3ClientFactory s3ClientFactory;

    public ListCallable(String key, S3Storage s3Storage, S3ClientFactory s3ClientFactory) {
        this.key = key;
        this.s3Storage = s3Storage;
        this.s3ClientFactory = s3ClientFactory;
    }

    @Override
    public List<String> call() {
        logger.info(String.format("Searching for files with prefix '%s'",key));

        S3Client s3Client = s3ClientFactory.getS3Client(s3Storage);

        ListObjectsRequest listObjects = ListObjectsRequest.builder()
                .bucket(s3Storage.getBucketName())
                .prefix(key)
                .build();

        // TODO: pagination?
        ListObjectsResponse res = s3Client.listObjects(listObjects);
        List<S3Object> s3Objects = res.contents();

        List<String> keys = s3Objects.stream()
                .map(S3Object::key)
                .collect(Collectors.toCollection(ArrayList::new));

        logger.info(String.format("Found on S3:\n'%s'", keys));

        return keys;
    }
}
