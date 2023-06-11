package ru.rerumu.s3.integration;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.S3Service;
import ru.rerumu.s3.S3ServiceFactory;
import ru.rerumu.s3.S3ServiceFactoryImpl;
import ru.rerumu.s3.impl.S3ServiceImpl;
import ru.rerumu.s3.models.S3Storage;
import software.amazon.awssdk.regions.Region;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ITS3ServiceUploadDownload {

    @Test
    void shouldUploadDownloadSmall(@TempDir Path tempDir) throws Exception{
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ERROR);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
//        logger.setLevel(Level.INFO);

        S3Storage s3Storage = new S3Storage(
                Region.AWS_GLOBAL,
                "test",
                "1111",
                "1111",
                Paths.get("level-0"),
                new URI("http://127.0.0.1:9090/"),
                "STANDARD"
        );

        S3ServiceFactory s3ServiceFactory = new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000,
                100_000_000_000L,
                tempDir,
                UUID.randomUUID()
        );

        byte[] data = new byte[1_000];
        new Random().nextBytes(data);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);

        String key = "TestBucket/TestPool/level-0/shouldUploadDownloadSmall__"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                + "/";

        s3Service.upload(bufferedInputStream,key);

        S3ServiceFactory s3ServiceFactoryDownload = new S3ServiceFactoryImpl();
        S3Service s3ServiceDownload = s3ServiceFactoryDownload.getS3Service(
                s3Storage,
                12_000_000,
                100_000_000_000L,
                tempDir,
                UUID.randomUUID()
        );

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        s3ServiceDownload.download(key,bufferedOutputStream);

        bufferedOutputStream.flush();
        byte[] dataDownloaded = byteArrayOutputStream.toByteArray();

        Assertions.assertArrayEquals(data,dataDownloaded);
    }

    @Test
    void shouldUploadDownloadBig(@TempDir Path tempDir) throws Exception{
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.ERROR);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
//        logger.setLevel(Level.INFO);

        S3Storage s3Storage = new S3Storage(
                Region.AWS_GLOBAL,
                "test",
                "1111",
                "1111",
                Paths.get("level-0"),
                new URI("http://127.0.0.1:9090/"),
                "STANDARD"
        );

        S3ServiceFactory s3ServiceFactory = new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                7_000_000,
                10_000_000L,
                tempDir,
                UUID.randomUUID()
        );

        byte[] data = new byte[24_000_000];
        new Random().nextBytes(data);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);

        String key = "TestBucket/TestPool/level-0/shouldUploadDownloadBig__"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                + "/";

        s3Service.upload(bufferedInputStream,key);

        S3ServiceFactory s3ServiceFactoryDownload = new S3ServiceFactoryImpl();
        S3Service s3ServiceDownload = s3ServiceFactoryDownload.getS3Service(
                s3Storage,
                7_000_000,
                10_000_000L,
                tempDir,
                UUID.randomUUID()
        );

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        s3ServiceDownload.download(key,bufferedOutputStream);

        bufferedOutputStream.flush();
        byte[] dataDownloaded = byteArrayOutputStream.toByteArray();

        Assertions.assertArrayEquals(data,dataDownloaded);
    }
}
