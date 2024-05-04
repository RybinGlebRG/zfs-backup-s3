package ru.rerumu.zfs_backup_s3.local_storage.services.impl;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.local_storage.exceptions.FileHitSizeLimitException;
import ru.rerumu.zfs_backup_s3.local_storage.exceptions.ZFSStreamEndedException;
import ru.rerumu.zfs_backup_s3.local_storage.factories.ZFSFileReaderFactory;
import ru.rerumu.zfs_backup_s3.local_storage.factories.ZFSFileWriterFactory;
import ru.rerumu.zfs_backup_s3.local_storage.services.ZFSFileReader;
import ru.rerumu.zfs_backup_s3.local_storage.services.ZFSFileWriter;
import ru.rerumu.zfs_backup_s3.s3.S3Service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsecutiveLocalStorageServiceTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Mock
    ZFSFileReaderFactory zfsFileReaderFactory;
    @Mock
    ZFSFileWriterFactory zfsFileWriterFactory;
    @Mock
    S3Service s3Service;
    String unique;
    Path tempDir;
    ConsecutiveLocalStorageService consecutiveLocalStorageService;

    @BeforeEach
    public void beforeEach() {
        unique = UUID.randomUUID().toString();
        tempDir = Paths.get("test");
        consecutiveLocalStorageService = new ConsecutiveLocalStorageService(
                zfsFileReaderFactory,
                zfsFileWriterFactory,
                s3Service,
                unique,
                tempDir
        );
    }

    @Test
    public void shouldSendExisting() {
        /*
            Creating test objects
         */
        String prefix = "prefix";
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.DEBUG);


        /*
        Mocking
         */
        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.list(tempDir)).thenAnswer(invocationOnMock -> {
                List<Path> pathsList = new ArrayList<>();
                pathsList.add(tempDir.resolve("file1"));
                pathsList.add(tempDir.resolve("file2"));
                pathsList.add(tempDir.resolve("file3"));
                return pathsList.stream();
            });


            /*
                Steps
             */
            consecutiveLocalStorageService.sendExisting(prefix);


            /*
                Asserts
             */
            InOrder inOrder = Mockito.inOrder(s3Service, Files.class);

            inOrder.verify(s3Service).upload(tempDir.resolve("file1"), "prefix");
            inOrder.verify(filesMockedStatic, () -> Files.delete(tempDir.resolve("file1")));

            inOrder.verify(s3Service).upload(tempDir.resolve("file2"), "prefix");
            inOrder.verify(filesMockedStatic, () -> Files.delete(tempDir.resolve("file2")));

            inOrder.verify(s3Service).upload(tempDir.resolve("file3"), "prefix");
            inOrder.verify(filesMockedStatic, () -> Files.delete(tempDir.resolve("file3")));

        }
    }

    @Test
    public void shouldSend() throws Exception {
        /*
            Creating test objects
         */
        BufferedInputStream bufferedInputStream = mock(BufferedInputStream.class);
        String prefix = "prefix";
        ZFSFileWriter zfsFileWriter = mock(ZFSFileWriter.class);


        /*
        Mocking
         */
        when(zfsFileWriterFactory.getZFSFileWriter(any())).thenReturn(zfsFileWriter);
        doThrow(FileHitSizeLimitException.class)
                .doThrow(ZFSStreamEndedException.class)
                .when(zfsFileWriter).write(any());
        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {


            /*
                Steps
             */
            consecutiveLocalStorageService.send(bufferedInputStream, prefix);


            /*
                Asserts
             */
            InOrder inOrder = inOrder(s3Service, zfsFileWriter, Files.class);

            inOrder.verify(zfsFileWriter, times(2)).write(any());
            inOrder.verify(s3Service).upload(eq(tempDir.resolve(unique+".part0")), anyString());
            inOrder.verify(filesMockedStatic, () -> Files.delete(eq(tempDir.resolve(unique+".part0"))));
            inOrder.verify(s3Service).upload(eq(tempDir.resolve(unique+".part1")), anyString());
            inOrder.verify(filesMockedStatic, () -> Files.delete(eq(tempDir.resolve(unique+".part1"))));

        }
    }

    @Test
    public void shouldReturnPresent(){
          /*
        Mocking
         */
        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.list(tempDir)).thenAnswer(invocationOnMock -> {
                List<Path> pathsList = new ArrayList<>();
                pathsList.add(tempDir.resolve("file1"));
                pathsList.add(tempDir.resolve("file2"));
                pathsList.add(tempDir.resolve("file3"));
                return pathsList.stream();
            });


            /*
                Steps
             */
            boolean res = consecutiveLocalStorageService.areFilesPresent();


            /*
                Asserts
             */
            Assertions.assertTrue(res);

        }
    }

    @Test
    public void shouldReturnNotPresent(){
          /*
        Mocking
         */
        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.list(tempDir)).thenAnswer(invocationOnMock -> {
                List<Path> pathsList = new ArrayList<>();
                return pathsList.stream();
            });


            /*
                Steps
             */
            boolean res = consecutiveLocalStorageService.areFilesPresent();


            /*
                Asserts
             */
            Assertions.assertFalse(res);

        }
    }

    @Test
    public void shouldReceiveFull() throws Exception{
        /*
            Creating test objects
         */
        ZFSFileReader zfsFileReader = mock(ZFSFileReader.class);
        String prefix = "prefix";
        BufferedOutputStream bufferedOutputStream = mock(BufferedOutputStream.class);


        /*
        Mocking
         */
        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.list(tempDir)).thenAnswer(invocationOnMock -> {
                List<Path> pathsList = new ArrayList<>();
                return pathsList.stream();
            });
            when(s3Service.list(anyString())).thenAnswer(invocationOnMock -> {
                List<String> keys = new ArrayList<>();
                keys.add("level-0/test-snapshot/random-name.part0");
                keys.add("level-0/test-snapshot/random-name.part1");
                keys.add("level-0/test-snapshot/random-name.part2");
                return keys;
            });
            when(zfsFileReaderFactory.getZFSFileReader(any(),any())).thenReturn(zfsFileReader);

            /*
                Steps
             */
            consecutiveLocalStorageService.receive(prefix, bufferedOutputStream);


            /*
                Asserts
             */
            InOrder inOrder = inOrder(s3Service, Files.class, zfsFileReaderFactory);

            inOrder.verify(s3Service).download(eq("level-0/test-snapshot/random-name.part0"),eq(tempDir.resolve("random-name.part0")));
            inOrder.verify(s3Service).download(eq("level-0/test-snapshot/random-name.part1"),eq(tempDir.resolve("random-name.part1")));
            inOrder.verify(s3Service).download(eq("level-0/test-snapshot/random-name.part2"),eq(tempDir.resolve("random-name.part2")));

            inOrder.verify(zfsFileReaderFactory).getZFSFileReader(any(), eq(tempDir.resolve("random-name.part0")));
            inOrder.verify(zfsFileReaderFactory).getZFSFileReader(any(), eq(tempDir.resolve("random-name.part1")));
            inOrder.verify(zfsFileReaderFactory).getZFSFileReader(any(), eq(tempDir.resolve("random-name.part2")));

            inOrder.verify(filesMockedStatic, () -> Files.delete(tempDir.resolve("random-name.part0")));
            inOrder.verify(filesMockedStatic, () -> Files.delete(tempDir.resolve("random-name.part1")));
            inOrder.verify(filesMockedStatic, () -> Files.delete(tempDir.resolve("random-name.part2")));

        }
    }

    @Test
    public void shouldReceivePartiallyLoaded() throws Exception{
        /*
            Creating test objects
         */
        ZFSFileReader zfsFileReader = mock(ZFSFileReader.class);
        String prefix = "prefix";
        BufferedOutputStream bufferedOutputStream = mock(BufferedOutputStream.class);


        /*
        Mocking
         */
        try (MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            filesMockedStatic.when(() -> Files.list(tempDir)).thenAnswer(invocationOnMock -> {
                List<Path> pathsList = new ArrayList<>();
                pathsList.add(tempDir.resolve("random-name.part0"));
                pathsList.add(tempDir.resolve("random-name.part1"));
                return pathsList.stream();
            });
            when(s3Service.list(anyString())).thenAnswer(invocationOnMock -> {
                List<String> keys = new ArrayList<>();
                keys.add("level-0/test-snapshot/random-name.part0");
                keys.add("level-0/test-snapshot/random-name.part1");
                keys.add("level-0/test-snapshot/random-name.part2");
                return keys;
            });
            when(zfsFileReaderFactory.getZFSFileReader(any(),any())).thenReturn(zfsFileReader);

            /*
                Steps
             */
            consecutiveLocalStorageService.receive(prefix, bufferedOutputStream);


            /*
                Asserts
             */
            InOrder inOrder = inOrder(s3Service, Files.class, zfsFileReaderFactory);

            inOrder.verify(s3Service).download(eq("level-0/test-snapshot/random-name.part2"),eq(tempDir.resolve("random-name.part2")));

            inOrder.verify(zfsFileReaderFactory).getZFSFileReader(any(), eq(tempDir.resolve("random-name.part0")));
            inOrder.verify(zfsFileReaderFactory).getZFSFileReader(any(), eq(tempDir.resolve("random-name.part1")));
            inOrder.verify(zfsFileReaderFactory).getZFSFileReader(any(), eq(tempDir.resolve("random-name.part2")));

            inOrder.verify(filesMockedStatic, () -> Files.delete(tempDir.resolve("random-name.part0")));
            inOrder.verify(filesMockedStatic, () -> Files.delete(tempDir.resolve("random-name.part1")));
            inOrder.verify(filesMockedStatic, () -> Files.delete(tempDir.resolve("random-name.part2")));

            inOrder.verifyNoMoreInteractions();

        }
    }
}
