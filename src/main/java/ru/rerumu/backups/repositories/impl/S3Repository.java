package ru.rerumu.backups.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.exceptions.S3MissesFileException;
import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.factories.S3ManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.repositories.RemoteBackupRepository;
import ru.rerumu.backups.services.S3Manager;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.json.JSONObject;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class S3Repository implements RemoteBackupRepository {

    private final List<S3Storage> storages;
    private final Logger logger = LoggerFactory.getLogger(S3Repository.class);
    private final S3ManagerFactory s3ManagerFactory;
    private final S3ClientFactory s3ClientFactory;
    private final S3Storage s3Storage;

    private final Path tmpDir;

    public S3Repository(
            final List<S3Storage> s3Storages,
            S3ManagerFactory S3ManagerFactory,
            S3ClientFactory s3ClientFactory,
            Path tmpDir) {
        this.storages = s3Storages;
        this.s3ManagerFactory = S3ManagerFactory;
        this.s3ClientFactory = s3ClientFactory;
        this.s3Storage = storages.get(0);
        this.tmpDir = tmpDir;
    }

    private void upload(final String datasetName, final Path path)
            throws
            IOException,
            NoSuchAlgorithmException,
            IncorrectHashException {
        if (storages.size() == 0) {
            throw new IllegalArgumentException();
        }
        for (S3Storage s3Storage : storages) {
            logger.info(String.format("Uploading file %s", path.toString()));
            String key = s3Storage.getPrefix().toString() + "/" + datasetName + "/" + path.getFileName().toString();
            logger.info(String.format("Target: %s", key));

            try (S3Client s3Client = s3ClientFactory.getS3Client(s3Storage);
                 BufferedInputStream bufferedInputStream = new BufferedInputStream(Files.newInputStream(path))) {

                S3Manager s3Manager = s3ManagerFactory.getUploadManager(
                        bufferedInputStream,
                        Files.size(path),
                        s3Storage,
                        key,
                        s3Client
                );
                s3Manager.run();

            }
        }
    }

    private boolean isDatasetExists(String datasetName) throws IncorrectHashException, IOException, NoSuchAlgorithmException {
        String key = s3Storage.getPrefix().toString() + "/_meta.json";
        Path target = Paths.get(tmpDir.toString(),"_meta.json");
        try(S3Client s3Client = s3ClientFactory.getS3Client(s3Storage)){
            S3Manager s3Manager = s3ManagerFactory.getDownloadManager(s3Storage,key,s3Client, target);
            s3Manager.run();
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
    public Path getNext(String datasetName) {
        // TODO: Write
        return null;
    }

    @Override
    public Path getNext(String datasetName, String filename) {
        // TODO: Write
        return null;
    }

    @Override
    public boolean isFileExists(final String datasetName, final String filename) {

        for (S3Storage s3Storage : storages) {
            try (S3Client s3Client = s3ClientFactory.getS3Client(s3Storage)) {

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
        }
        return false;
    }

}
