package ru.rerumu.backups.io.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.io.S3Loader;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
//import software.amazon.awssdk.transfer.s3.*;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.*;
import software.amazon.awssdk.services.s3.model.*;

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
    public void addStorage(S3Storage s3Storage){
        storages.add(s3Storage);
    }

    private String getMD5(Path path) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try(InputStream inputStream = Files.newInputStream(path);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)){
            byte[] buf = bufferedInputStream.readAllBytes();
            byte[] digest = md.digest(buf);
            logger.info(String.format("Pure MD5: '%s'",new String(digest, StandardCharsets.UTF_8)));
            String md5 = Base64.getEncoder().encodeToString(digest);
            logger.info(String.format("Base64 MD5: '%s'",md5));
            return md5;
        }
    }

    @Override
    public void upload(String datasetName, Path path) throws IOException, NoSuchAlgorithmException {
        // TODO: Check we have storages added
        for (S3Storage s3Storage:storages ) {
            logger.info(String.format("Uploading file %s",path.toString()));
            String key = s3Storage.getPrefix().toString()+"/"+datasetName+"/"+path.getFileName().toString();
            logger.info(String.format("Target: %s",key));
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
                    .contentMD5(md5)
                    .build();

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest,path);
            logger.info(String.format("Uploaded file '%s'. ETag='%s'",path.toString(),putObjectResponse.eTag()));
        }
    }
}
