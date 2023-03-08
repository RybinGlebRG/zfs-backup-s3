package ru.rerumu.backups.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.exceptions.FileHitSizeLimitException;
import ru.rerumu.backups.exceptions.ZFSStreamEndedException;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.repositories.S3Repository;
import ru.rerumu.backups.services.ZFSFileWriter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class S3StreamRepositoryImpl implements S3Repository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Path tempDir;

    private final S3Repository s3Repository;

    private final ZFSFileWriterFactory zfsFileWriterFactory;

    public S3StreamRepositoryImpl(Path tempDir, S3Repository s3Repository, ZFSFileWriterFactory zfsFileWriterFactory) {
        this.tempDir = tempDir;
        this.s3Repository = s3Repository;
        this.zfsFileWriterFactory = zfsFileWriterFactory;
    }

    @Override
    public void add(String prefix, Path path) {

    }

    @Override
    public List<String> getAll(String prefix) {
        return null;
    }

    @Override
    public String getOne(String prefix) {
        return null;
    }

    public void add(String prefix, BufferedInputStream bufferedInputStream)
            throws IOException,
            CompressorException,
            EncryptException
    {
        int n = 0;
        while (true) {
            Path newFilePath = tempDir.resolve(prefix+".part"+n);
            ZFSFileWriter zfsFileWriter = zfsFileWriterFactory.getZFSFileWriter(newFilePath);
            n++;
            try {
                zfsFileWriter.write(bufferedInputStream);
            } catch (FileHitSizeLimitException e) {
                // TODO: metadata?
                s3Repository.add(prefix,newFilePath);
                logger.debug(String.format(
                        "File '%s' processed",
                        newFilePath));
            } catch (ZFSStreamEndedException e) {
                s3Repository.add(prefix,newFilePath);
                logger.debug(String.format(
                        "File '%s' processed",
                        newFilePath));
                logger.info("End of stream. Exiting");
                break;
            }

        }
    }
}
