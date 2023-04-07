package ru.rerumu.backups.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.factories.S3ManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.models.meta.PartMeta;
import ru.rerumu.backups.repositories.RemoteBackupRepository;
import ru.rerumu.backups.services.s3.S3Manager;
import ru.rerumu.backups.services.impl.ListManager;
import software.amazon.awssdk.services.s3.S3Client;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class S3Repository implements RemoteBackupRepository {

    private final Logger logger = LoggerFactory.getLogger(S3Repository.class);
    private final S3ManagerFactory s3ManagerFactory;
    private final S3ClientFactory s3ClientFactory;
    private final S3Storage s3Storage;

    public S3Repository(
            final List<S3Storage> s3Storages,
            S3ManagerFactory S3ManagerFactory,
            S3ClientFactory s3ClientFactory) {
        this.s3ManagerFactory = S3ManagerFactory;
        this.s3ClientFactory = s3ClientFactory;
        this.s3Storage = s3Storages.get(0);
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

    private void download(String key, Path target) throws IOException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException {
        if (!isFileExists(key)){
            throw new S3MissesFileException();
        }
        try (S3Client s3Client = s3ClientFactory.getS3Client(s3Storage);) {

            S3Manager s3Manager = s3ManagerFactory.getDownloadManager(s3Storage,key,s3Client,target);
            s3Manager.run();

        }
    }

    private void download(String key, Path target, String storedMd5Hex) throws IOException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException {
        if (!isFileExists(key)){
            throw new S3MissesFileException();
        }
        try (S3Client s3Client = s3ClientFactory.getS3Client(s3Storage);) {

            S3Manager s3Manager = s3ManagerFactory.getDownloadManager(s3Storage,key,s3Client,target,storedMd5Hex);
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

    @Override
    public void add(String prefix, Path path)
            throws
            IOException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            S3MissesFileException {
        String key = s3Storage.getPrefix().toString() + "/" + prefix + path.getFileName().toString();
        upload(path, key);
        logger.info(String.format("Checking sent file '%s'", path.getFileName().toString()));
        if (!isFileExists(key)) {
            logger.error(String.format("File '%s' not found on S3", path.getFileName().toString()));
            throw new S3MissesFileException();
        }
        logger.info(String.format("File '%s' found on S3", path.getFileName().toString()));
    }

    @Override
    public Path getPart(String datasetName, String partName, Path targetDir, PartMeta partMeta)
            throws IOException, NoSuchAlgorithmException, IncorrectHashException, NoPartFoundException {
        String key = s3Storage.getPrefix().toString() + "/" + datasetName + "/" + partName;
        Path path = targetDir.resolve(partName);
        try {
            download(key, path, partMeta.getMd5Hex());
        } catch (S3MissesFileException e){
            logger.warn(String.format("Part '%s' of dataset '%s' not found on s3",partName,datasetName));
            throw new NoPartFoundException();
        }
        return path;
    }

    @Override
    public Path getBackupMeta(Path targetDir)
            throws IOException, NoSuchAlgorithmException, IncorrectHashException, NoBackupMetaException {
        Path path = targetDir.resolve("_meta.json");
        try {
            download(s3Storage.getPrefix()+"/_meta.json",path);
        } catch (S3MissesFileException e){
            logger.warn("Backup meta is not found on S3");
            throw new NoBackupMetaException();
        }
        return path;
    }

    @Override
    public Path getDatasetMeta(String datasetName, Path targetDir) throws IOException, NoSuchAlgorithmException, IncorrectHashException, NoDatasetMetaException {
        Path path = targetDir.resolve("_meta.json");
        try {
            download(s3Storage.getPrefix()+"/"+datasetName+"/_meta.json",path);
        } catch (S3MissesFileException e){
            logger.warn(String.format("Metadata for dataset '%s' not found on S3",datasetName));
            throw new NoDatasetMetaException();
        }
        return path;
    }

    @Override
    public boolean isFileExists(final String datasetName, final String filename) throws IOException, NoSuchAlgorithmException, IncorrectHashException {
        String key = s3Storage.getPrefix().toString() + "/" + datasetName + "/" + filename;

        return isFileExists(key);
    }

}
