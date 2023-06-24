package ru.rerumu.s3.operations;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.zfs_backup_s3.s3.impl.operations.MultipartUploadCallable;
import ru.rerumu.zfs_backup_s3.s3.services.S3RequestService;
import ru.rerumu.zfs_backup_s3.s3.services.impl.S3RequestServiceMock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestMultipartUploadCallable {

    @Mock
    S3RequestServiceMock s3RequestService;

    @Test
    void shouldThrowException(@TempDir Path tempDir) throws Exception {
        Path path = tempDir.resolve(UUID.randomUUID().toString());
        Files.createFile(path);

        when(s3RequestService.createMultipartUpload("test-key"))
                .thenThrow(RuntimeException.class);


        Callable<Void> callable = new MultipartUploadCallable(
                path,
                "test-key",
                1_000,
                s3RequestService
        );

        Assertions.assertThrows(RuntimeException.class, callable::call);
        verifyNoMoreInteractions(s3RequestService);
    }

    @Test
    void shouldThrowException1(@TempDir Path tempDir)throws Exception{
        Path path = tempDir.resolve(UUID.randomUUID().toString());
        byte[] data = new byte[1000];
        new Random().nextBytes(data);

        Files.write(
                path,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        when(s3RequestService.createMultipartUpload("test-key"))
                .thenReturn("test-upload");
        when(s3RequestService.uploadPart(eq("test-key"),eq("test-upload"),anyInt(),any()))
                .thenThrow(RuntimeException.class);


        Callable<Void> callable = new MultipartUploadCallable(
                path,
                "test-key",
                1_000,
                s3RequestService
        );

        Assertions.assertThrows(RuntimeException.class, callable::call);

        verify(s3RequestService).abortMultipartUpload("test-key","test-upload");
        verifyNoMoreInteractions(s3RequestService);
    }
}
