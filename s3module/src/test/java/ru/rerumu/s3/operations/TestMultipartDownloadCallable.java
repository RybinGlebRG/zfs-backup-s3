package ru.rerumu.s3.operations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.s3.exceptions.IncorrectHashException;
import ru.rerumu.zfs_backup_s3.s3.impl.operations.MultipartDownloadCallable;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.services.impl.requests.models.ListObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestMultipartDownloadCallable {

    @Mock
    S3RequestService s3RequestService;

    @Test
    void shouldThrowException(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve(UUID.randomUUID().toString());
        Files.createFile(path);

        byte[] md5 = new byte[32];
        new Random().nextBytes(md5);


        when(s3RequestService.getMetadata("test-key"))
                .thenReturn(new ListObject("test-key","12345",100L));
        when(s3RequestService.getObjectRange("test-key",0L,100L,path))
                .thenReturn(md5);


        Callable<Void> callable = new MultipartDownloadCallable(
                path,
                "test-key",
                1_000,
                s3RequestService
        );

        Assertions.assertThrows(IncorrectHashException.class, callable::call);
        verifyNoMoreInteractions(s3RequestService);
    }
}
