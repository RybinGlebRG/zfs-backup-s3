package ru.rerumu.backups.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.S3Loader;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.transfer.s3.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class S3LoaderImpl implements S3Loader {

    private final List<S3Storage> storages = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(S3LoaderImpl.class);

    @Override
    public void addStorage(S3Storage s3Storage){
        storages.add(s3Storage);
    }

    @Override
    public void upload(String datasetName, Path path){
        // TODO: Check we have storages added
        for (S3Storage s3Storage:storages ) {
            logger.info(String.format("Uploading file %s",path.toString()));
            String key = s3Storage.getPrefix().toString()+"/"+datasetName+"/"+path.getFileName().toString();
            logger.info(String.format("Target: %s",key));

            S3ClientConfiguration s3ClientConfiguration = S3ClientConfiguration.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(s3Storage.getCredentials()))
                    .region(s3Storage.getRegion())
                    .endpointOverride(s3Storage.getEndpoint())
                    .maxConcurrency(1)
                    .build();

            S3TransferManager s3TransferManager = S3TransferManager.builder()
                    .s3ClientConfiguration(s3ClientConfiguration)
                    .build();

            FileUpload fileUpload = s3TransferManager
                    .uploadFile(request -> request
                            .source(path)
                            .putObjectRequest(p -> p
                                .bucket(s3Storage.getBucketName())
                                .key(key)
                                .storageClass(s3Storage.getStorageClass())
                            )
                    );

            CompletedFileUpload completedfileUpload = fileUpload.completionFuture().join();
            logger.info(completedfileUpload.response().toString());
        }
    }
}
