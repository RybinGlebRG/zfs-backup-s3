package ru.rerumu.backups.repositories.impl;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.exceptions.S3MissesFileException;
import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.factories.S3ManagerFactory;
import ru.rerumu.backups.models.BackupMeta;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.repositories.RemoteBackupRepository;
import ru.rerumu.backups.services.S3Manager;
import ru.rerumu.backups.services.impl.ListManager;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import org.json.JSONObject;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public class S3Repository implements RemoteBackupRepository {

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
        this.s3ManagerFactory = S3ManagerFactory;
        this.s3ClientFactory = s3ClientFactory;
        this.s3Storage = s3Storages.get(0);
        this.tmpDir = tmpDir;
    }

    private void upload(Path path, String key)
            throws
            IOException,
            NoSuchAlgorithmException,
            IncorrectHashException {

        logger.info(String.format("Uploading file %s", path.toString()));
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

    private void download(String key, Path target) throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        try (S3Client s3Client = s3ClientFactory.getS3Client(s3Storage);) {

            S3Manager s3Manager = s3ManagerFactory.getDownloadManager(s3Storage,key,s3Client,target);
            s3Manager.run();

        }
    }

    private boolean isFileExists(String key) throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        try(S3Client s3Client = s3ClientFactory.getS3Client(s3Storage)){
            ListManager listManager = s3ManagerFactory.getListManager(s3Storage,key,s3Client);
            listManager.run();
        } catch (S3MissesFileException e ){
            return false;
        }
        return true;
    }

    private void addDatasetMeta(String datasetName) throws IOException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException {
        if (!isFileExists(s3Storage.getPrefix()+"/_meta.json")){
            BackupMeta backupMeta = new BackupMeta();
            backupMeta.addDataset(datasetName);

            Path path = tmpDir.resolve("_meta.json");
            Files.writeString(
                    path,
                    backupMeta.toJSONObject().toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            upload(path,s3Storage.getPrefix()+"/_meta.json");
            Files.delete(path);
        } else {
            Path path = tmpDir.resolve("_meta.json");
            download(s3Storage.getPrefix()+"/_meta.json",path);

            BackupMeta backupMeta = new BackupMeta(new JSONObject(path));
            backupMeta.addDataset(datasetName);

            Files.writeString(
                    path,
                    backupMeta.toJSONObject().toString(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            upload(path,s3Storage.getPrefix()+"/_meta.json");
            Files.delete(path);
        }
    }

    public void addPartMeta(String datasetName, String partName, long partSize){
        // TODO: Write
    }

    @Override
    public void add(final String datasetName, final Path path)
            throws
            IOException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            S3MissesFileException {
        String key = s3Storage.getPrefix().toString() + "/" + datasetName + "/" + path.getFileName().toString();
        upload(path, key);
        logger.info(String.format("Checking sent file '%s'", path.getFileName().toString()));
        if (!isFileExists(datasetName, path.getFileName().toString())) {
            logger.error(String.format("File '%s' not found on S3", path.getFileName().toString()));
            throw new S3MissesFileException();
        }
        // TODO: Check already added
        addDatasetMeta(datasetName);
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
    public boolean isFileExists(final String datasetName, final String filename) throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        String key = s3Storage.getPrefix().toString() + "/" + datasetName + "/" + filename;

        return isFileExists(key);
    }

}
