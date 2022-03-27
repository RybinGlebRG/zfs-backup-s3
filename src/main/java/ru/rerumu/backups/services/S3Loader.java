package ru.rerumu.backups.services;

import ru.rerumu.backups.models.S3Storage;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.transfer.s3.S3ClientConfiguration;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class S3Loader {

    private static final Long PART_SIZE = 104857600L; // 100MB
    private final List<S3Storage> storages = new ArrayList<>();

    public void addStorage(S3Storage s3Storage){
        storages.add(s3Storage);
    }

    public void upload(Path path){
        // TODO: Check we have storages added
        for (S3Storage item:storages ) {
            S3Client s3 = prepareS3Client(item);
            String key = item.getPrefix().toString()+"/"+path.getFileName().toString();
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(item.getBucketName())
                    .key(key)
                    .storageClass(item.getStorageClass())
                    .build();
            s3.putObject(objectRequest, path);

        }

    }

//    public void uploadV2(Path path){
//        for (S3Storage s3Storage:storages ) {
//            S3ClientConfiguration s3ClientConfiguration = S3ClientConfiguration.builder()
//                    .credentialsProvider(StaticCredentialsProvider.create(s3Storage.getCredentials()))
//                    .region(s3Storage.getRegion())
//                    .maxConcurrency(1)
//                    .build();
//            S3TransferManager s3TransferManager = S3TransferManager.builder()
//                    .s3ClientConfiguration(s3ClientConfiguration)
//                    .
//        }
//    }

    private S3Client prepareS3Client(S3Storage s3Storage){
        S3Client s3 = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(s3Storage.getCredentials()))
                .region(s3Storage.getRegion())
                .endpointOverride(s3Storage.getEndpoint())
                .build();
        return s3;
    }

    public void multipartUpload(Path path) throws IOException{
        // TODO: Check we have storages added
        for (S3Storage item:storages ) {
            S3Client s3 = prepareS3Client(item);
            String key = item.getPrefix().toString()+"/"+path.getFileName().toString();
            var multipartUploadRequest = CreateMultipartUploadRequest.builder()
                    .bucket(item.getBucketName())
                    .key(key)
                    .storageClass(item.getStorageClass())
                    .build();
            var multipartUpload = s3.createMultipartUpload(multipartUploadRequest);
            String uploadId = multipartUpload.uploadId();
            int partNumber = 0;
            String abortRuleId = multipartUpload.abortRuleId();

            // TODO: Limit one part by size
            try(BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path))){
                Path partPath = Paths.get(path.getParent().toString(),UUID.randomUUID().toString());
                try(BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(Files.newOutputStream(partPath))){
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = bufferedInputStream.read(buf)) >= 0) {
                        bufferedOutputStream.write(buf, 0, len);
                    }
                }
                UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                        .partNumber(partNumber)
                        .uploadId(uploadId)
                        .bucket(item.getBucketName())
                        .key(key)
                        .build();
                UploadPartResponse uploadPartResponse = s3.uploadPart(uploadPartRequest,partPath);
                // TODO: Wait until loading is finished
            }


        }
    }
}
