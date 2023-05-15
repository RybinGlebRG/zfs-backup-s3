package ru.rerumu.s3.operations;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.impl.operations.MultipartUploadCallable;
import ru.rerumu.s3.services.S3RequestService;
import ru.rerumu.s3.services.impl.requests.models.UploadPartResult;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestMultipartUploadCallable {

    @Mock
    S3RequestService s3RequestService;

    @Mock
    CreateMultipartUploadResponse createMultipartUploadResponse;

    @Mock
    UploadPartResponse uploadPartResponse;

    @Test
    void shouldUpload(@TempDir Path tempDir) throws Exception {
        Path target = tempDir.resolve(UUID.randomUUID().toString());
        String uploadId = "12345";
        CompletedPart completedPart1 = CompletedPart.builder().partNumber(1).eTag("1").build();
        CompletedPart completedPart2 = CompletedPart.builder().partNumber(2).eTag("2").build();
        CompletedPart completedPart3 = CompletedPart.builder().partNumber(3).eTag("3").build();

        byte[] md5_1 = new byte[0];
        byte[] md5_2 = new byte[0];
        byte[] md5_3 = new byte[0];

        byte[] data = new byte[2000];
        new Random().nextBytes(data);

        Files.write(
                target,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );


        when(s3RequestService.createMultipartUpload(anyString())).thenReturn(uploadId);

        when(s3RequestService.uploadPart(eq("test-key"), eq("12345"), eq(1), any()))
                .thenReturn(new UploadPartResult(md5_1, completedPart1));
        when(s3RequestService.uploadPart(eq("test-key"), eq("12345"), eq(2), any()))
                .thenReturn(new UploadPartResult(md5_2, completedPart2));
        when(s3RequestService.uploadPart(eq("test-key"), eq("12345"), eq(3), any()))
                .thenReturn(new UploadPartResult(md5_3, completedPart3));


        new MultipartUploadCallable(
                target,
                "test-key",
                700,
                s3RequestService
        ).call();


        verify(s3RequestService).completeMultipartUpload(
                List.of(completedPart1, completedPart2, completedPart3),
                "test-key",
                "12345",
                List.of(md5_1, md5_2, md5_3)
        );
        verifyNoMoreInteractions(s3RequestService);
    }

    @Test
    void shouldAbort(@TempDir Path tempDir) throws Exception{
        Path target = tempDir.resolve(UUID.randomUUID().toString());
        String uploadId = "12345";
        CompletedPart completedPart1 = CompletedPart.builder().partNumber(1).eTag("1").build();

        byte[] md5_1 = new byte[0];

        byte[] data = new byte[2000];
        new Random().nextBytes(data);

        Files.write(
                target,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );


        when(s3RequestService.createMultipartUpload(anyString())).thenReturn(uploadId);

        when(s3RequestService.uploadPart(eq("test-key"), eq("12345"), eq(1), any()))
                .thenReturn(new UploadPartResult(md5_1, completedPart1));
        when(s3RequestService.uploadPart(eq("test-key"), eq("12345"), eq(2), any()))
                .thenThrow(RuntimeException.class);


        Callable<Void> callable = new MultipartUploadCallable(
                target,
                "test-key",
                700,
                s3RequestService
        );

        Assertions.assertThrows(RuntimeException.class, callable::call);


        verify(s3RequestService).abortMultipartUpload("test-key","12345");
        verifyNoMoreInteractions(s3RequestService);
    }
}
