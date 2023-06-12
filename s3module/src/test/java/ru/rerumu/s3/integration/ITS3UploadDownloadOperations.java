package ru.rerumu.s3.integration;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.s3.factories.S3ClientFactory;
import ru.rerumu.zfs_backup_s3.s3.factories.impl.S3ClientFactoryImpl;
import ru.rerumu.zfs_backup_s3.s3.impl.operations.MultipartDownloadCallable;
import ru.rerumu.zfs_backup_s3.s3.impl.operations.MultipartUploadCallable;
import ru.rerumu.zfs_backup_s3.s3.impl.operations.OnepartUploadCallable;
import ru.rerumu.zfs_backup_s3.s3.models.S3Storage;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.services.impl.S3RequestServiceImpl;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.CallableSupplierFactory;
import ru.rerumu.zfs_backup_s3.utils.callables.impl.CallableExecutorImpl;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class ITS3UploadDownloadOperations {

    @Test
    void shouldUploadDownloadOnepart(@TempDir Path tempDir) throws Exception {
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
        S3ClientFactory s3ClientFactory = new S3ClientFactoryImpl(List.of(s3Storage));
        S3RequestService s3RequestService = new S3RequestServiceImpl(
                new CallableExecutorImpl(),
                // TODO: Thread safe?
                new CallableSupplierFactory(
                        s3ClientFactory,
                        s3Storage
                ));

        String key = "TestBucket/TestPool/level-0/shouldUploadDownloadOnepart__"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                + "/" + UUID.randomUUID() + ".part0";

        Path pathUpload = tempDir.resolve(UUID.randomUUID().toString());
        byte[] data = new byte[1_000_000];
        new Random().nextBytes(data);
        Files.write(
                pathUpload,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
        OnepartUploadCallable uploadCallable = new OnepartUploadCallable(pathUpload, key, s3RequestService);
        uploadCallable.call();


        Path pathDownload = tempDir.resolve(UUID.randomUUID().toString());
        MultipartDownloadCallable downloadCallable = new MultipartDownloadCallable(
                pathDownload,
                key,
                12_000_000,
                s3RequestService
        );
        downloadCallable.call();


        byte[] dataUpload = Files.readAllBytes(pathUpload);
        byte[] dataDownload = Files.readAllBytes(pathDownload);

        Assertions.assertArrayEquals(dataUpload, dataDownload);
    }

    @Test
    void shouldUploadDownloadMultipart(@TempDir Path tempDir) throws Exception {
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
        S3ClientFactory s3ClientFactory = new S3ClientFactoryImpl(List.of(s3Storage));
        S3RequestService s3RequestService = new S3RequestServiceImpl(
                new CallableExecutorImpl(),
                // TODO: Thread safe?
                new CallableSupplierFactory(
                        s3ClientFactory,
                        s3Storage
                ));

        String key = "TestBucket/TestPool/level-0/shouldUploadDownloadMultipart__"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
                + "/" + UUID.randomUUID() + ".part0";

        Path pathUpload = tempDir.resolve(UUID.randomUUID().toString());
        byte[] data = new byte[30_000_000];
        new Random().nextBytes(data);
        Files.write(
                pathUpload,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
        MultipartUploadCallable uploadCallable = new MultipartUploadCallable(
                pathUpload,
                key,
                12_000_000,
                s3RequestService
        );
        uploadCallable.call();


        Path pathDownload = tempDir.resolve(UUID.randomUUID().toString());
        MultipartDownloadCallable downloadCallable = new MultipartDownloadCallable(
                pathDownload,
                key,
                12_000_000,
                s3RequestService
        );
        downloadCallable.call();


        byte[] dataUpload = Files.readAllBytes(pathUpload);
        byte[] dataDownload = Files.readAllBytes(pathDownload);

        Assertions.assertArrayEquals(dataUpload, dataDownload);
    }
}
