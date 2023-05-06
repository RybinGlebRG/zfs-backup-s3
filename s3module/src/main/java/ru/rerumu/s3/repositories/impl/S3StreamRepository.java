package ru.rerumu.s3.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.utils.ZFSFileReader;
import ru.rerumu.s3.utils.ZFSFileWriter;
import ru.rerumu.s3.exceptions.FileHitSizeLimitException;
import ru.rerumu.s3.exceptions.ZFSStreamEndedException;
import ru.rerumu.s3.factories.ZFSFileReaderFactory;
import ru.rerumu.s3.factories.ZFSFileWriterFactory;
import ru.rerumu.s3.repositories.S3Repository;
import ru.rerumu.s3.utils.FileManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class S3StreamRepository implements S3Repository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final S3Repository s3Repository;

    private final ZFSFileWriterFactory zfsFileWriterFactory;
    private final ZFSFileReaderFactory zfsFileReaderFactory;

    private final FileManager fileManager;

    public S3StreamRepository(S3Repository s3Repository, ZFSFileWriterFactory zfsFileWriterFactory, ZFSFileReaderFactory zfsFileReaderFactory, FileManager fileManager) {
        this.s3Repository = s3Repository;
        this.zfsFileWriterFactory = zfsFileWriterFactory;
        this.zfsFileReaderFactory = zfsFileReaderFactory;
        this.fileManager = fileManager;
    }

    @Override
    public void add(String prefix, Path path) {
        s3Repository.add(prefix, path);
    }

    @Override
    public List<String> listAll(String prefix) {
        return s3Repository.listAll(prefix);
    }

    @Override
    public void getOne(String prefix, Path targetPath) {
        s3Repository.getOne(prefix, targetPath);
    }

    public void add(String prefix, BufferedInputStream bufferedInputStream) {
        try {
            int n = 0;
            while (true) {
                Path newFilePath = fileManager.getNew(null, ".part" + n++);
                try (ZFSFileWriter zfsFileWriter = zfsFileWriterFactory.getZFSFileWriter(newFilePath)) {
                    zfsFileWriter.write(bufferedInputStream);
                } catch (FileHitSizeLimitException e) {
                    s3Repository.add(prefix, newFilePath);
                    Files.delete(newFilePath);
                    logger.debug(String.format("File '%s' processed", newFilePath));
                } catch (ZFSStreamEndedException e) {
                    s3Repository.add(prefix, newFilePath);
                    Files.delete(newFilePath);
                    logger.debug(String.format("File '%s' processed", newFilePath));
                    logger.info("End of stream. Exiting");
                    break;
                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }

    public void getAll(BufferedOutputStream bufferedOutputStream, String prefix){
        try {


            List<String> keys = s3Repository.listAll(prefix);
            for (String key : keys) {
                Path part = fileManager.getNew(null, "-" + Paths.get(key).getFileName());
                s3Repository.getOne(key, part);
                ZFSFileReader zfsFileReader = zfsFileReaderFactory.getZFSFileReader(
                        bufferedOutputStream, part
                );
                zfsFileReader.read();
                fileManager.delete(part);
            }
        } catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new RuntimeException(e);
        }
    }
}
