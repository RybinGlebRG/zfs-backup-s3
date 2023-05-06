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
import ru.rerumu.s3.models.S3Storage;
import software.amazon.awssdk.regions.Region;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ITS3UploadDownload {


    @Test
    void shouldUploadDownloadOnepart(@TempDir Path tempDir) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
//        logger.setLevel(Level.INFO);

        S3Storage s3Storage = new S3Storage(
                Region.AWS_GLOBAL,
                "test",
                "1111",
                "1111",
                Paths.get("level-0"),
                new URI("http://localhost:9090/"),
                "STANDARD"
        );

        byte[] data = new byte[3_000];

        new Random().nextBytes(data);


        S3ServiceFactory s3ServiceFactory = new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000,
                100_000_000_000L,
                tempDir
        );
        s3Service.upload(
                new BufferedInputStream(new ByteArrayInputStream(data)),
                "TestBucket/TestPool/level-0/zfs-backup-s3__2023-05-06T184000/"
        );


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        S3Service s3Service1 = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000,
                100_000_000_000L,
                tempDir
        );
        s3Service1.download(
                "TestBucket/TestPool/level-0/zfs-backup-s3__2023-05-06T184000/",
                bufferedOutputStream
        );

        // TODO: Flush?
        byte[] actual = byteArrayOutputStream.toByteArray();

        Assertions.assertArrayEquals(data,actual);
    }

    @Test
    void shouldUploadDownloadMultipart(@TempDir Path tempDir) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
//        logger.setLevel(Level.INFO);

        S3Storage s3Storage = new S3Storage(
                Region.AWS_GLOBAL,
                "test",
                "1111",
                "1111",
                Paths.get("level-0"),
                new URI("http://localhost:9090/"),
                "STANDARD"
        );

        byte[] data = new byte[30_000_000];

        new Random().nextBytes(data);


        S3ServiceFactory s3ServiceFactory = new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000,
                100_000_000_000L,
                tempDir
        );
        s3Service.upload(
                new BufferedInputStream(new ByteArrayInputStream(data)),
                "TestBucket/TestPool/level-0/zfs-backup-s3__2023-05-06T184000/"
        );


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        S3Service s3Service1 = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000,
                100_000_000_000L,
                tempDir
        );
        s3Service1.download(
                "TestBucket/TestPool/level-0/zfs-backup-s3__2023-05-06T184000/",
                bufferedOutputStream
        );

        // TODO: Flush?
        byte[] actual = byteArrayOutputStream.toByteArray();

        Assertions.assertArrayEquals(data,actual);
    }

}
