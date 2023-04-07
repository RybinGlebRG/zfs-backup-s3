package ru.rerumu.backups.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.factories.ZFSFileReaderFactory;
import ru.rerumu.backups.factories.ZFSFileWriterFactory;
import ru.rerumu.backups.repositories.S3Repository;
import ru.rerumu.backups.services.TempPathGenerator;
import ru.rerumu.backups.services.ZFSFileReader;
import ru.rerumu.backups.services.ZFSFileWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class S3StreamRepositoryImpl implements S3Repository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private final S3Repository s3Repository;

    private final ZFSFileWriterFactory zfsFileWriterFactory;
    private final ZFSFileReaderFactory zfsFileReaderFactory;

    private final TempPathGenerator tempPathGenerator;

    public S3StreamRepositoryImpl(S3Repository s3Repository, ZFSFileWriterFactory zfsFileWriterFactory, ZFSFileReaderFactory zfsFileReaderFactory, TempPathGenerator tempPathGenerator) {
        this.s3Repository = s3Repository;
        this.zfsFileWriterFactory = zfsFileWriterFactory;
        this.zfsFileReaderFactory = zfsFileReaderFactory;
        this.tempPathGenerator = tempPathGenerator;
    }

    @Override
    public void add(String prefix, Path path)
            throws S3MissesFileException,
            IOException,
            NoSuchAlgorithmException,
            IncorrectHashException {
        s3Repository.add(prefix, path);
    }

    @Override
    public List<String> listAll(String prefix) {
        return s3Repository.listAll(prefix);
    }

    @Override
    public Path getOne(String prefix) {
        return s3Repository.getOne(prefix);
    }

    public void add(String prefix, BufferedInputStream bufferedInputStream) throws Exception {
        int n = 0;
        while (true) {
            Path newFilePath = tempPathGenerator.generateConsistent(null,".part"+ n++);
            try(ZFSFileWriter zfsFileWriter = zfsFileWriterFactory.getZFSFileWriter(newFilePath)) {
                zfsFileWriter.write(bufferedInputStream);
            } catch (FileHitSizeLimitException e) {
                s3Repository.add(prefix, newFilePath);
                Files.delete(newFilePath);
                logger.debug(String.format("File '%s' processed",newFilePath));
            } catch (ZFSStreamEndedException e) {
                s3Repository.add(prefix, newFilePath);
                Files.delete(newFilePath);
                logger.debug(String.format("File '%s' processed",newFilePath));
                logger.info("End of stream. Exiting");
                break;
            }

        }
    }

    public void getAll(BufferedOutputStream bufferedOutputStream, String prefix)
            throws CompressorException, IOException, ClassNotFoundException, EncryptException {
        List<String> files = s3Repository.listAll(prefix);
        for (String file : files) {
            Path part = s3Repository.getOne(prefix+"/"+file);
            ZFSFileReader zfsFileReader = zfsFileReaderFactory.getZFSFileReader(
                    bufferedOutputStream, part
            );
            zfsFileReader.read();
        }
    }
}
