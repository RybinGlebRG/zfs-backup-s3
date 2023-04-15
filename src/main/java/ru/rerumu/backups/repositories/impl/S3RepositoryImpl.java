package ru.rerumu.backups.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.exceptions.S3MissesFileException;
import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.factories.S3ManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.repositories.S3Repository;
import ru.rerumu.backups.services.s3.impl.ListManager;
import ru.rerumu.backups.services.s3.S3Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;


public class S3RepositoryImpl implements S3Repository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final S3Storage s3Storage;
    private final S3ManagerFactory s3ManagerFactory;
    private final S3ClientFactory s3ClientFactory;

    private final S3Service s3Service;

    public S3RepositoryImpl(S3Storage s3Storage, S3ManagerFactory s3ManagerFactory, S3ClientFactory s3ClientFactory, S3Service s3Service) {
        this.s3Storage = s3Storage;
        this.s3ManagerFactory = s3ManagerFactory;
        this.s3ClientFactory = s3ClientFactory;
        this.s3Service = s3Service;
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

    // TODO: Metadata?
    @Override
    public void add(String prefix, Path path)
            throws IOException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            S3MissesFileException {
        String key = s3Storage.getPrefix().toString() + "/" + prefix + path.getFileName().toString();
        s3Service.upload(path,key);
        logger.info(String.format("Checking sent file '%s'", path.getFileName().toString()));
        if (!isFileExists(key)) {
            logger.error(String.format("File '%s' not found on S3", path.getFileName().toString()));
            throw new S3MissesFileException();
        }
        logger.info(String.format("File '%s' found on S3", path.getFileName().toString()));
    }

    @Override
    public List<String> listAll(String prefix) {
        // TODO: write
        return null;
    }

    @Override
    public Path getOne(String prefix) {
        // TODO: write
        return null;
    }
}
