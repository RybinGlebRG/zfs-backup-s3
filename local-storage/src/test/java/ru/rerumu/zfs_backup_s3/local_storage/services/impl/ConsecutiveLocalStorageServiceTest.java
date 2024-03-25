package ru.rerumu.zfs_backup_s3.local_storage.services.impl;

import ch.qos.logback.classic.Level;
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
import ru.rerumu.zfs_backup_s3.local_storage.factories.ZFSFileReaderFactory;
import ru.rerumu.zfs_backup_s3.local_storage.factories.ZFSFileWriterFactory;
import ru.rerumu.zfs_backup_s3.s3.S3Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.mockStatic;

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
    public void beforeEach(){
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
    public void shouldSendExisting(){
        /*
            Creating test objects
         */
        String prefix = "prefix";
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.DEBUG);


        /*
        Mocking
         */
        try(MockedStatic<Files> filesMockedStatic = mockStatic(Files.class)) {
            logger.info("Here4");
            filesMockedStatic.when(()->Files.list(tempDir)).thenAnswer(invocationOnMock -> {
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
            inOrder.verify(filesMockedStatic,()->Files.delete(tempDir.resolve("file1")));

            inOrder.verify(s3Service).upload(tempDir.resolve("file2"), "prefix");
            inOrder.verify(filesMockedStatic,()->Files.delete(tempDir.resolve("file2")));

            inOrder.verify(s3Service).upload(tempDir.resolve("file3"), "prefix");
            inOrder.verify(filesMockedStatic,()->Files.delete(tempDir.resolve("file3")));



        }

    }
}
