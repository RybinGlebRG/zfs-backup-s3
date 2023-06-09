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
import ru.rerumu.s3.factories.S3ClientFactory;
import ru.rerumu.s3.factories.impl.S3ClientFactoryImpl;
import ru.rerumu.s3.factories.impl.ZFSFileReaderFactoryImpl;
import ru.rerumu.s3.factories.impl.ZFSFileWriterFactoryImpl;
import ru.rerumu.s3.impl.S3CallableFactory;
import ru.rerumu.s3.impl.S3CallableFactoryImpl;
import ru.rerumu.s3.impl.S3ServiceImpl;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.s3.services.S3RequestService;
import ru.rerumu.s3.services.impl.S3RequestServiceImpl;
import ru.rerumu.s3.services.impl.requests.CallableSupplierFactory;
import ru.rerumu.s3.utils.impl.FileManagerImpl;
import ru.rerumu.utils.callables.impl.CallableExecutorImpl;
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
import java.util.List;
import java.util.Random;
import java.util.UUID;

// TODO: rewrite - wrong service
@ExtendWith(MockitoExtension.class)
public class ITS3UploadDownload {

    @Test
    void shouldUploadDownloadOnepart(@TempDir Path tempDir) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.DEBUG);
        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
        logger.setLevel(Level.INFO);
        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
        logger.setLevel(Level.INFO);
        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
        logger.setLevel(Level.INFO);
        S3ServiceFactoryImpl s3ServiceFactory = new S3ServiceFactoryImpl();

        S3Storage s3Storage = new S3Storage(
                Region.AWS_GLOBAL,
                "test",
                "1111",
                "1111",
                Paths.get("level-0"),
                new URI("http://127.0.0.1:9090/"),
                "STANDARD"
        );

        S3Service serviceUpload = s3ServiceFactory.getS3Service(
            s3Storage,
                12_000_000,
                100_000_000_000L,
                tempDir,
                UUID.randomUUID()
        );

        byte[] data = new byte[3_000];
        new Random().nextBytes(data);

        String key = "TestBucket/TestPool/level-0/shouldUploadDownloadOnepart__"
                +LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                +"/";

        serviceUpload.upload(
                new BufferedInputStream(new ByteArrayInputStream(data)),
                key
        );

        S3Service serviceDownload = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000,
                100_000_000_000L,
                tempDir,
                UUID.randomUUID()
        );

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

        serviceDownload.download(
                key,
                bufferedOutputStream
        );

        bufferedOutputStream.flush();
        byte[] actual = byteArrayOutputStream.toByteArray();

        Assertions.assertArrayEquals(data,actual);
    }

    @Test
    void shouldUploadDownloadMultipart(@TempDir Path tempDir) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.DEBUG);
        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
        logger.setLevel(Level.INFO);
        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
        logger.setLevel(Level.INFO);
        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
        logger.setLevel(Level.INFO);

        S3Storage s3Storage = new S3Storage(
                Region.AWS_GLOBAL,
                "test",
                "1111",
                "1111",
                Paths.get("level-0"),
                new URI("http://127.0.0.1:9090/"),
                "STANDARD"
        );
        String key = "TestBucket/TestPool/level-0/shouldUploadDownloadMultipart__"
                +LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                +"/";


        byte[] data = new byte[30_000_000];
        new Random().nextBytes(data);


        S3ServiceFactory s3ServiceFactory = new S3ServiceFactoryImpl();
        S3Service s3Service = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000,
                100_000_000_000L,
                tempDir,
                UUID.randomUUID()
        );
        s3Service.upload(
                new BufferedInputStream(new ByteArrayInputStream(data)),
                key
        );


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
        S3Service s3Service1 = s3ServiceFactory.getS3Service(
                s3Storage,
                12_000_000,
                100_000_000_000L,
                tempDir,
                UUID.randomUUID()
        );
        s3Service1.download(
                key,
                bufferedOutputStream
        );

        bufferedOutputStream.flush();
        byte[] actual = byteArrayOutputStream.toByteArray();

        Assertions.assertArrayEquals(data,actual);
    }

}
