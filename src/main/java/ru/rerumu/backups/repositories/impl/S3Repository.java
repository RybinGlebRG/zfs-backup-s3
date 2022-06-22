package ru.rerumu.backups.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.exceptions.S3MissesFileException;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.repositories.RemoteBackupRepository;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import org.apache.commons.codec.binary.Hex;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class S3Repository implements RemoteBackupRepository {

    private final List<S3Storage> storages;
    private final Logger logger = LoggerFactory.getLogger(S3Repository.class);

    public S3Repository(final List<S3Storage> s3Storages) {
        this.storages = s3Storages;
    }

    private String getMD5(final Path path)
            throws NoSuchAlgorithmException,
            IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream inputStream = Files.newInputStream(path);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            String md5 = '"' + Hex.encodeHexString(md.digest(bufferedInputStream.readAllBytes())) + '"';
            logger.info(String.format("Hex MD5: '%s'", md5));
            return md5;
        }
    }

    private void upload(final String datasetName, final Path path)
            throws
            IOException,
            NoSuchAlgorithmException,
            IncorrectHashException {
        if (storages.size()==0){
            throw new IllegalArgumentException();
        }
        for (S3Storage s3Storage : storages) {
            logger.info(String.format("Uploading file %s", path.toString()));
            String key = s3Storage.getPrefix().toString() + "/" + datasetName + "/" + path.getFileName().toString();
            logger.info(String.format("Target: %s", key));
            String md5 = getMD5(path);


            S3Client s3Client = S3Client.builder()
                    .region(s3Storage.getRegion())
                    .endpointOverride(s3Storage.getEndpoint())
                    .credentialsProvider(StaticCredentialsProvider.create(s3Storage.getCredentials()))
                    .build();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Storage.getBucketName())
                    .key(key)
                    .storageClass(s3Storage.getStorageClass())
                    .build();

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, path);
            String eTag = putObjectResponse.eTag();
            logger.info(String.format("Uploaded file '%s'", path.toString()));
            logger.info(String.format("ETag='%s'", eTag));
            if (!(eTag.equals(md5))) {
                throw new IncorrectHashException();
            }
        }
    }

    @Override
    public void add(final String datasetName, final Path path)
            throws
            IOException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            S3MissesFileException {
        upload(datasetName, path);
        logger.info(String.format("Checking sent file '%s'", path.getFileName().toString()));
        if (!isFileExists(datasetName, path.getFileName().toString())) {
            logger.error(String.format("File '%s' not found on S3", path.getFileName().toString()));
            throw new S3MissesFileException();
        }
        logger.info(String.format("File '%s' found on S3", path.getFileName().toString()));
    }

    @Override
    public boolean isFileExists(final String datasetName, final String filename) {

        for (S3Storage s3Storage : storages) {
            S3Client s3Client = S3Client.builder()
                    .region(s3Storage.getRegion())
                    .endpointOverride(s3Storage.getEndpoint())
                    .credentialsProvider(StaticCredentialsProvider.create(s3Storage.getCredentials()))
                    .build();

            String prefix = s3Storage.getPrefix().toString() + "/" + datasetName + "/" + filename;

            ListObjectsRequest listObjects = ListObjectsRequest.builder()
                    .bucket(s3Storage.getBucketName())
                    .prefix(prefix)
                    .build();

            ListObjectsResponse res = s3Client.listObjects(listObjects);
            List<S3Object> s3Objects = res.contents();
            logger.info(String.format("Found on S3:\n'%s'", s3Objects));

            if (s3Objects.size() > 1) {
                throw new IllegalArgumentException();
            }

            for (S3Object s3Object : s3Objects) {
                String tmp = Paths.get(s3Object.key()).getFileName().toString();
                if (tmp.equals(filename)) {
                    return true;
                }
            }

        }
        return false;
    }

}
