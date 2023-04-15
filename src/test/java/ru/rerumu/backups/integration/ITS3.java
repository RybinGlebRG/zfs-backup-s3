package ru.rerumu.backups.integration;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.factories.S3ClientFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.s3.S3Service;
import ru.rerumu.backups.services.s3.impl.ListCallable;
import ru.rerumu.backups.services.s3.impl.OnepartUploadCallable;
import ru.rerumu.backups.services.s3.impl.S3ServiceImpl;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ITS3 {

    @Mock
    S3ClientFactory s3ClientFactory;

    @Test
    void shouldUpload(@TempDir Path tempDir ) throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.TRACE);
        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
        logger.setLevel(Level.INFO);

        S3Storage s3Storage = new S3Storage(
                Region.of(System.getProperty("region")),
                System.getProperty("bucket"),
                System.getProperty("keyId"),
                System.getProperty("secretKey"),
                Paths.get("level-0"),
                new URI(System.getProperty("endpoint")),
                "STANDARD"
        );

        when(s3ClientFactory.getS3Client(any()))
                .thenReturn(
                        S3Client.builder()
                                .region(s3Storage.getRegion())
                                .endpointOverride(s3Storage.getEndpoint())
                                .credentialsProvider(StaticCredentialsProvider.create(s3Storage.getCredentials()))
                                .build()
                );

        Files.writeString(
                tempDir.resolve("test.txt"),
                "Test",
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );

        var onepartUpload = new OnepartUploadCallable(
                tempDir.resolve("test.txt"),
                "test.txt",
                s3Storage,
                s3ClientFactory
        );

        onepartUpload.call();


        Callable<List<String>> listCallable = new ListCallable(
            "test.txt",
                s3Storage,
                s3ClientFactory
        );

        List<String> res = listCallable.call();

        Assertions.assertEquals(List.of("test.txt"),res);


    }
}
