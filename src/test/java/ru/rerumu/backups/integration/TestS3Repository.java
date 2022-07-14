package ru.rerumu.backups.integration;

import ch.qos.logback.classic.Level;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.exceptions.S3MissesFileException;
import ru.rerumu.backups.factories.impl.S3ManagerFactoryImpl;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.repositories.impl.S3Repository;
import software.amazon.awssdk.regions.Region;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Random;

@Disabled
public class TestS3Repository {

    @Test
    void shouldSendOnepart(@TempDir Path tempDir) throws URISyntaxException, IOException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.TRACE);
        S3Repository s3Repository = new S3Repository(List.of(
                new S3Storage(
                        Region.of(System.getProperty("region")),
                        System.getProperty("bucket"),
                        System.getProperty("keyId"),
                        System.getProperty("secretKey"),
                        Paths.get("level-0"),
                        new URI(System.getProperty("endpoint")),
                        "STANDARD"
                )),
                new S3ManagerFactoryImpl(500_000_000)
        );


        byte[] srcByte = new byte[5431];
        new Random().nextBytes(srcByte);
        String datasetName = RandomStringUtils.random(10, true, false)
                +"-"+RandomStringUtils.random(10, true, false);
        String generatedString = datasetName+"@auto-20200321-173000__"
                +datasetName+"@auto-20200322-173000.part0";
        String generatedString1 = datasetName+"@auto-20200321-173000__"
                +datasetName+"@auto-20200322-173000.part1";

        Path path = Files.createFile(tempDir.resolve(generatedString));
        Path path1 = Files.createFile(tempDir.resolve(generatedString1));

        try(BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(Files.newOutputStream(path))){
            bufferedOutputStream.write(srcByte);
        }

        s3Repository.add(datasetName,path);
        s3Repository.add(datasetName,path1);

        Assertions.assertTrue(s3Repository.isFileExists(datasetName,generatedString));
        Assertions.assertTrue(s3Repository.isFileExists(datasetName,generatedString1));
    }

    @Test
    void shouldSendMultipart(@TempDir Path tempDir) throws URISyntaxException, IOException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.TRACE);
        S3Repository s3Repository = new S3Repository(List.of(
                new S3Storage(
                        Region.of(System.getProperty("region")),
                        System.getProperty("bucket"),
                        System.getProperty("keyId"),
                        System.getProperty("secretKey"),
                        Paths.get("level-0"),
                        new URI(System.getProperty("endpoint")),
                        "STANDARD"
                )),
                new S3ManagerFactoryImpl(6_291_456)
        );


        byte[] srcByte = new byte[7_291_456];
        new Random().nextBytes(srcByte);
        String datasetName = RandomStringUtils.random(10, true, false)
                +"-"+RandomStringUtils.random(10, true, false);
        String generatedString = datasetName+"@auto-20200321-173000__"
                +datasetName+"@auto-20200322-173000.part0";

        Path path = Files.createFile(tempDir.resolve(generatedString));

        try(BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(Files.newOutputStream(path))){
            bufferedOutputStream.write(srcByte);
        }

        s3Repository.add(datasetName,path);

        Assertions.assertTrue(s3Repository.isFileExists(datasetName,generatedString));
    }

    @Test
    void shouldNotFind(@TempDir Path tempDir) throws URISyntaxException, IOException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.TRACE);
        S3Repository s3Repository = new S3Repository(List.of(
                new S3Storage(
                        Region.of(System.getProperty("region")),
                        System.getProperty("bucket"),
                        System.getProperty("keyId"),
                        System.getProperty("secretKey"),
                        Paths.get("level-0"),
                        new URI(System.getProperty("endpoint")),
                        "STANDARD"
                )),
                new S3ManagerFactoryImpl(500_000_000)
        );


        byte[] srcByte = new byte[5431];
        new Random().nextBytes(srcByte);
        String datasetName = RandomStringUtils.random(10, true, false)
                +"-"+RandomStringUtils.random(10, true, false);
        String generatedString = datasetName+"@auto-20200321-173000__"
                +datasetName+"@auto-20200322-173000.part0";

        Path path = Files.createFile(tempDir.resolve(generatedString));

        try(BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(Files.newOutputStream(path))){
            bufferedOutputStream.write(srcByte);
        }

        s3Repository.add(datasetName,path);

        boolean isExists = s3Repository.isFileExists(datasetName,"test");
        Assertions.assertFalse(isExists);
    }
}
