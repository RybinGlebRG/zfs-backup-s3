package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.exceptions.S3MissesFileException;
import ru.rerumu.backups.models.S3Storage;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class ListManager  {
    private final Logger logger = LoggerFactory.getLogger(ListManager.class);
    private final S3Storage s3Storage;
    private final String key;
    private final S3Client s3Client;

    public ListManager(S3Storage s3Storage, String key, S3Client s3Client) {
        this.s3Storage = s3Storage;
        this.key = key;
        this.s3Client = s3Client;
    }

    public void run() throws IOException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException {
        String filename = Paths.get(key).getFileName().toString();

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

        boolean isExists = false;

        for (S3Object s3Object : s3Objects) {
            String tmp = Paths.get(s3Object.key()).getFileName().toString();
            if (tmp.equals(filename)) {
                isExists = true;
            }
        }

        if (!isExists){
            throw new S3MissesFileException();
        }
    }
}
