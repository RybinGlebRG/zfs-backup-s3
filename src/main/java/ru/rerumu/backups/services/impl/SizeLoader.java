package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.S3Storage;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.List;

public class SizeLoader {
    private final Logger logger = LoggerFactory.getLogger(SizeLoader.class);

    private final S3Storage s3Storage;
    private final String key;
    private final S3Client s3Client;

    public SizeLoader(S3Storage s3Storage, String key, S3Client s3Client){
        this.s3Storage =s3Storage;
        this.key = key;
        this.s3Client = s3Client;
    }

    public Long getSize(){
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

        return s3Objects.get(0).size();
    }
}
