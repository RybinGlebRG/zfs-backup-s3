package ru.rerumu.backups.io.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.io.S3Loader;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.transfer.s3.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;
import org.apache.commons.codec.binary.Hex;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class S3LoaderImpl implements S3Loader {

    private final List<S3Storage> storages = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(S3LoaderImpl.class);

    @Override
    public void addStorage(S3Storage s3Storage) {
        storages.add(s3Storage);
    }

    private String getMD5(Path path) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream inputStream = Files.newInputStream(path);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            String md5 = '"' + Hex.encodeHexString(md.digest(bufferedInputStream.readAllBytes())) + '"';
            logger.info(String.format("Hex MD5: '%s'", md5));
            return md5;
        }
    }

    @Override
    public void upload(String datasetName, Path path) throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        // TODO: Check we have storages added
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

    public List<String> objectsList(String prefix) {
        List<String> objects = new ArrayList<>();
        for (S3Storage s3Storage : storages) {
            S3Client s3Client = S3Client.builder()
                    .region(s3Storage.getRegion())
                    .endpointOverride(s3Storage.getEndpoint())
                    .credentialsProvider(StaticCredentialsProvider.create(s3Storage.getCredentials()))
                    .build();

            ListObjectsRequest listObjects = ListObjectsRequest.builder()
                    .bucket(s3Storage.getBucketName())
                    .prefix(prefix)
                    .build();

            ListObjectsResponse res = s3Client.listObjects(listObjects);
            List<S3Object> s3Objects = res.contents();


            for (S3Object s3Object : s3Objects) {
                objects.add(s3Object.key());
            }

        }
        return objects;
    }
}
