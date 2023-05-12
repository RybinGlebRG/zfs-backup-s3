package ru.rerumu.s3.integration;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.s3.factories.S3ClientFactory;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.s3.impl.operations.ListCallable;
import ru.rerumu.s3.impl.operations.OnepartUploadCallable;
import ru.rerumu.utils.callables.impl.CallableExecutorImpl;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


// TODO: rewrite
@ExtendWith(MockitoExtension.class)
public class ITS3List {

//    @Mock
//    S3ClientFactory s3ClientFactory;
//
//
//    @Test
//    void shouldListAll(@TempDir Path tempDir ) throws Exception {
//        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
//        logger.setLevel(Level.INFO);
////        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
////        logger.setLevel(Level.INFO);
////        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
////        logger.setLevel(Level.INFO);
////        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
////        logger.setLevel(Level.INFO);
//
//        String key = "TestBucket/TestPool/level-0/zfs-backup-s3__"
//                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
//                +"/"
//                +UUID.randomUUID().toString();
////        String prefix = UUID.randomUUID().toString();
//
//        S3Storage s3Storage = new S3Storage(
//                Region.AWS_GLOBAL,
//                "test",
//                "1111",
//                "1111",
//                Paths.get("level-0"),
//                new URI("http://127.0.0.1:9090/"),
//                "STANDARD"
//        );
//
//        when(s3ClientFactory.getS3Client(any()))
//                .thenReturn(
//                        S3Client.builder()
//                                .region(s3Storage.getRegion())
//                                .endpointOverride(s3Storage.getEndpoint())
//                                .credentialsProvider(StaticCredentialsProvider.create(s3Storage.getCredentials()))
//                                .build()
//                );
//
//        byte[] data1 = new byte[3_000];
//        byte[] data2 = new byte[4_000];
//        byte[] data3 = new byte[5_000];
//
//        new Random().nextBytes(data1);
//        new Random().nextBytes(data2);
//        new Random().nextBytes(data3);
//
//        Files.write(
//                tempDir.resolve("test1.txt"),
//                data1,
//                StandardOpenOption.CREATE,
//                StandardOpenOption.TRUNCATE_EXISTING,
//                StandardOpenOption.WRITE
//        );
//
//        Files.write(
//                tempDir.resolve("test2.txt"),
//                data2,
//                StandardOpenOption.CREATE,
//                StandardOpenOption.TRUNCATE_EXISTING,
//                StandardOpenOption.WRITE
//        );
//
//        Files.write(
//                tempDir.resolve("test3.txt"),
//                data3,
//                StandardOpenOption.CREATE,
//                StandardOpenOption.TRUNCATE_EXISTING,
//                StandardOpenOption.WRITE
//        );
//
//        new OnepartUploadCallable(
//                tempDir.resolve("test1.txt"),
//                key+".part1",
//                s3Storage,
//                s3ClientFactory,
//                new CallableExecutorImpl(), s3RequestService).call();
//        new OnepartUploadCallable(
//                tempDir.resolve("test2.txt"),
//                key+".part2",
//                s3Storage,
//                s3ClientFactory,
//                new CallableExecutorImpl(), s3RequestService).call();
//        new OnepartUploadCallable(
//                tempDir.resolve("test3.txt"),
//                key+".part3",
//                s3Storage,
//                s3ClientFactory,
//                new CallableExecutorImpl(), s3RequestService).call();
//
//
//
//
//
//        Set<String> res = new HashSet<>(new ListCallable(
//                key,
//                s3Storage,
//                s3ClientFactory,
//                new CallableExecutorImpl()).call());
//
//
//        Assertions.assertEquals(Set.of(key+".part1",key+".part2",key+".part3"),res);
//
//
//    }
//
//    @Test
//    void shouldListOne(@TempDir Path tempDir ) throws Exception {
////        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
////        logger.setLevel(Level.INFO);
////        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
////        logger.setLevel(Level.INFO);
////        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
////        logger.setLevel(Level.INFO);
////        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
////        logger.setLevel(Level.INFO);
//
//        String key = "TestBucket/TestPool/level-0/zfs-backup-s3__"
//                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
//                +"/"
//                +UUID.randomUUID().toString();
////        String prefix = UUID.randomUUID().toString();
//
//        S3Storage s3Storage = new S3Storage(
//                Region.AWS_GLOBAL,
//                "test",
//                "1111",
//                "1111",
//                Paths.get("level-0"),
//                new URI("http://127.0.0.1:9090/"),
//                "STANDARD"
//        );
//
//
//        when(s3ClientFactory.getS3Client(any()))
//                .thenReturn(
//                        S3Client.builder()
//                                .region(s3Storage.getRegion())
//                                .endpointOverride(s3Storage.getEndpoint())
//                                .credentialsProvider(StaticCredentialsProvider.create(s3Storage.getCredentials()))
//                                .build()
//                );
//
//        byte[] data1 = new byte[3_000];
//        byte[] data2 = new byte[4_000];
//        byte[] data3 = new byte[5_000];
//
//        new Random().nextBytes(data1);
//        new Random().nextBytes(data2);
//        new Random().nextBytes(data3);
//
//        Files.write(
//                tempDir.resolve("test1.txt"),
//                data1,
//                StandardOpenOption.CREATE,
//                StandardOpenOption.TRUNCATE_EXISTING,
//                StandardOpenOption.WRITE
//        );
//
//        Files.write(
//                tempDir.resolve("test2.txt"),
//                data2,
//                StandardOpenOption.CREATE,
//                StandardOpenOption.TRUNCATE_EXISTING,
//                StandardOpenOption.WRITE
//        );
//
//        Files.write(
//                tempDir.resolve("test3.txt"),
//                data3,
//                StandardOpenOption.CREATE,
//                StandardOpenOption.TRUNCATE_EXISTING,
//                StandardOpenOption.WRITE
//        );
//
//        new OnepartUploadCallable(
//                tempDir.resolve("test1.txt"),
//                key+".part1",
//                s3Storage,
//                s3ClientFactory,
//                new CallableExecutorImpl(), s3RequestService).call();
//        new OnepartUploadCallable(
//                tempDir.resolve("test2.txt"),
//                key+".part2",
//                s3Storage,
//                s3ClientFactory,
//                new CallableExecutorImpl(), s3RequestService).call();
//        new OnepartUploadCallable(
//                tempDir.resolve("test3.txt"),
//                key+".part3",
//                s3Storage,
//                s3ClientFactory,
//                new CallableExecutorImpl(), s3RequestService).call();
//
//
//
//
//
//        Set<String> res = new HashSet<>(new ListCallable(
//                key+".part2",
//                s3Storage,
//                s3ClientFactory,
//                new CallableExecutorImpl()).call());
//
//
//        Assertions.assertEquals(Set.of(key+".part2"),res);
//
//
//    }
//
//    @Test
//    void shouldNotFind(@TempDir Path tempDir ) throws Exception {
////        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
////        logger.setLevel(Level.INFO);
////        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.interceptor.ExecutionInterceptorChain.class);
////        logger.setLevel(Level.INFO);
////        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.core.internal.io.AwsChunkedEncodingInputStream.class);
////        logger.setLevel(Level.INFO);
////        logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(software.amazon.awssdk.auth.signer.Aws4Signer.class);
////        logger.setLevel(Level.INFO);
//        String key = "TestBucket/TestPool/level-0/zfs-backup-s3__"
//                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss"))
//                +"/"
//                +UUID.randomUUID().toString();
////        String prefix = UUID.randomUUID().toString();
//
//        S3Storage s3Storage = new S3Storage(
//                Region.AWS_GLOBAL,
//                "test",
//                "1111",
//                "1111",
//                Paths.get("level-0"),
//                new URI("http://127.0.0.1:9090/"),
//                "STANDARD"
//        );
//
//        when(s3ClientFactory.getS3Client(any()))
//                .thenReturn(
//                        S3Client.builder()
//                                .region(s3Storage.getRegion())
//                                .endpointOverride(s3Storage.getEndpoint())
//                                .credentialsProvider(StaticCredentialsProvider.create(s3Storage.getCredentials()))
//                                .build()
//                );
//
//        byte[] data1 = new byte[3_000];
//        byte[] data2 = new byte[4_000];
//        byte[] data3 = new byte[5_000];
//
//        new Random().nextBytes(data1);
//        new Random().nextBytes(data2);
//        new Random().nextBytes(data3);
//
//        Files.write(
//                tempDir.resolve("test1.txt"),
//                data1,
//                StandardOpenOption.CREATE,
//                StandardOpenOption.TRUNCATE_EXISTING,
//                StandardOpenOption.WRITE
//        );
//
//        Files.write(
//                tempDir.resolve("test2.txt"),
//                data2,
//                StandardOpenOption.CREATE,
//                StandardOpenOption.TRUNCATE_EXISTING,
//                StandardOpenOption.WRITE
//        );
//
//        Files.write(
//                tempDir.resolve("test3.txt"),
//                data3,
//                StandardOpenOption.CREATE,
//                StandardOpenOption.TRUNCATE_EXISTING,
//                StandardOpenOption.WRITE
//        );
//
//        new OnepartUploadCallable(
//                tempDir.resolve("test1.txt"),
//                key+".part1",
//                s3Storage,
//                s3ClientFactory,
//                new CallableExecutorImpl(), s3RequestService).call();
//        new OnepartUploadCallable(
//                tempDir.resolve("test2.txt"),
//                key+".part2",
//                s3Storage,
//                s3ClientFactory,
//                new CallableExecutorImpl(), s3RequestService).call();
//        new OnepartUploadCallable(
//                tempDir.resolve("test3.txt"),
//                key+".part3",
//                s3Storage,
//                s3ClientFactory,
//                new CallableExecutorImpl(), s3RequestService).call();
//
//
//
//
//
//        Set<String> res = new HashSet<>(new ListCallable(
//                key+".part4",
//                s3Storage,
//                s3ClientFactory,
//                new CallableExecutorImpl()).call());
//
//
//        Assertions.assertEquals(0, res.size());
//
//
//    }

}
