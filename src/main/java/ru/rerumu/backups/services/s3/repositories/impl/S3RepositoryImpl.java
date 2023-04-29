package ru.rerumu.backups.services.s3.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.exceptions.S3MissesFileException;
import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.factories.S3ManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.s3.repositories.S3Repository;
import ru.rerumu.backups.services.s3.impl.ListManager;
import ru.rerumu.backups.services.s3.S3Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;


public class S3RepositoryImpl implements S3Repository {
    private static final String PART_SUFFIX = ".part";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final S3Service s3Service;

    public S3RepositoryImpl(S3Storage s3Storage, S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @Override
    public void add(String prefix, Path path)
            throws IOException,
            NoSuchAlgorithmException,
            IncorrectHashException,
            S3MissesFileException {
        String key = prefix + path.getFileName().toString();
        s3Service.upload(path,key);
    }

    @Override
    public List<String> listAll(String prefix) {
        List<String> res = s3Service.list(prefix);
        res.sort(
                Comparator.comparing(
                        item -> Integer.valueOf(item.substring(item.lastIndexOf(PART_SUFFIX)+PART_SUFFIX.length()))
                )
        );
        return res;
    }

    @Override
    public void getOne(String prefix, Path targetPath) {
        s3Service.download(prefix,targetPath);
    }
}
