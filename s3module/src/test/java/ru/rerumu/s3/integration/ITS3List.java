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
import ru.rerumu.s3.impl.operations.OnepartUploadCallable;
import ru.rerumu.s3.models.S3Storage;
import software.amazon.awssdk.regions.Region;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;


@ExtendWith(MockitoExtension.class)
public class ITS3List {

//    @Mock
//    S3ClientFactory s3ClientFactory;
//
//
    @Test
    void shouldListAll(@TempDir Path tempDir ) throws Exception {
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
                new URI("http://127.0.0.1:9090/"),
                "STANDARD"
        );

        S3ServiceFactory s3ServiceFactory = new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                8_000_000,
                100_000_000_000L,
                tempDir,
                UUID.randomUUID()
        );

        byte[] data = new byte[20_000_000];

        new Random().nextBytes(data);

        String key = "TestBucket/TestPool/level-0/shouldUploadDownloadOnepart__"
                +LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                +"/";

        s3Service.upload(
                new BufferedInputStream(new ByteArrayInputStream(data)),
                key
        );


        S3Service s3ServiceList = s3ServiceFactory.getS3Service(
                s3Storage,
                8_000_000,
                100_000_000_000L,
                tempDir,
                UUID.randomUUID()
        );

        List<String> res = s3ServiceList.list(key);

        Assertions.assertEquals(3,res.size());

        for(int i=0; i<res.size();i++) {
            Assertions.assertTrue(res.get(i).matches(".*\\.part" + i));
        }
    }

    @Test
    void shouldListOne(@TempDir Path tempDir ) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
//        logger.setLevel(Level.INFO);

        UUID uuid = UUID.randomUUID();

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
                8_000_000,
                100_000_000_000L,
                tempDir,
                uuid
        );

        byte[] data = new byte[20_000_000];

        new Random().nextBytes(data);

        String key = "TestBucket/TestPool/level-0/shouldUploadDownloadOnepart__"
                +LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                +"/";

        s3Service.upload(
                new BufferedInputStream(new ByteArrayInputStream(data)),
                key
        );


        S3Service s3ServiceList = s3ServiceFactory.getS3Service(
                s3Storage,
                8_000_000,
                100_000_000_000L,
                tempDir,
                uuid
        );

        List<String> res = s3ServiceList.list(key+uuid+".part2");

        Assertions.assertEquals(1,res.size());

        Assertions.assertEquals(key+uuid+".part2",res.get(0));
    }


    @Test
    void shouldNotFind(@TempDir Path tempDir ) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
//        logger.setLevel(Level.INFO);
//        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
//        logger.setLevel(Level.INFO);

        UUID uuid = UUID.randomUUID();

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
                8_000_000,
                100_000_000_000L,
                tempDir,
                uuid
        );

        byte[] data = new byte[20_000_000];

        new Random().nextBytes(data);

        String key = "TestBucket/TestPool/level-0/shouldUploadDownloadOnepart__"
                +LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                +"/";

        s3Service.upload(
                new BufferedInputStream(new ByteArrayInputStream(data)),
                key
        );


        S3Service s3ServiceList = s3ServiceFactory.getS3Service(
                s3Storage,
                8_000_000,
                100_000_000_000L,
                tempDir,
                uuid
        );

        List<String> res = s3ServiceList.list(key+uuid+".part4");

        Assertions.assertEquals(0,res.size());

    }
}
