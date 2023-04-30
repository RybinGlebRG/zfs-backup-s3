package ru.rerumu.backups.services.s3.factories.impl;

import ru.rerumu.backups.services.s3.factories.S3ClientFactory;
import ru.rerumu.backups.services.s3.models.S3Storage;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class S3ClientFactoryImpl implements S3ClientFactory {
    private final Map<S3Storage,S3Client> map = new HashMap<>();

    public S3ClientFactoryImpl(List<S3Storage> storageList) {
        for (S3Storage s3Storage: storageList){
            S3Client s3Client = S3Client.builder()
                    .region(s3Storage.getRegion())
                    .endpointOverride(s3Storage.getEndpoint())
                    .credentialsProvider(StaticCredentialsProvider.create(s3Storage.getCredentials()))
                    .build();
            map.put(s3Storage,s3Client);
        }

    }

    @Override
    public S3Client getS3Client(S3Storage s3Storage) {
        return map.get(s3Storage);
    }
}
