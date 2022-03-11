package ru.rerumu.backups.services;

import ru.rerumu.backups.models.S3Storage;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class S3Loader {

    private final List<S3Storage> storages = new ArrayList<>();

    public void addStorage(S3Storage s3Storage){
        storages.add(s3Storage);
    }

    public void upload(Path path){
        // TODO: Check we have storages added
        for (S3Storage item:storages ) {
            S3Client s3 = S3Client.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(item.getCredentials()))
                    .region(item.getRegion())
                    .endpointOverride(item.getEndpoint())
                    .build();
            String key = item.getPrefix().toString()+"/"+path.getFileName().toString();
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(item.getBucketName())
                    .key(key)
                    .storageClass(item.getStorageClass())
                    .build();
            s3.putObject(objectRequest, path);

        }

    }
}
