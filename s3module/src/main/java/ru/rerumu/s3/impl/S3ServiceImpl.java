package ru.rerumu.s3.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.S3Service;
import ru.rerumu.s3.factories.S3CallableFactory;
import ru.rerumu.s3.repositories.S3Repository;
import ru.rerumu.s3.repositories.impl.S3StreamRepository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

public class S3ServiceImpl implements S3Service {
    private final static Long DELAY = 10L;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final S3Repository s3Repository;

    private final S3StreamRepository s3StreamRepository;

    public S3ServiceImpl( S3Repository s3Repository, S3StreamRepository s3StreamRepository) {
        this.s3Repository = s3Repository;
        this.s3StreamRepository = s3StreamRepository;
    }

//    @Override
//    public void upload(Path sourcePath, String s3Key) {
//        s3Repository.add(s3Key,sourcePath);
//    }

    @Override
    public void upload(BufferedInputStream bufferedInputStream, String key) {
        s3StreamRepository.add(key,bufferedInputStream);
    }

//    @Override
//    public void download(String key, Path path) {
//        s3Repository.getOne(key,path);
//    }

    @Override
    public void download(String prefix, BufferedOutputStream bufferedOutputStream) {
        s3StreamRepository.getAll(bufferedOutputStream,prefix);
    }

    @Override
    public List<String> list(String prefix) {
        List<String> keys = s3Repository.listAll(prefix);
        return keys;
    }

}
