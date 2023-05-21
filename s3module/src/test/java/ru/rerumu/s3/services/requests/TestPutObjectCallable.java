package ru.rerumu.s3.services.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.services.impl.requests.PutObjectCallable;
import ru.rerumu.utils.MD5;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestPutObjectCallable {

    @Mock
    S3Client s3Client;

    @Mock
    PutObjectResponse putObjectResponse;

    @Test
    void shouldCall(@TempDir Path tempDir) throws Exception{
        Path source = tempDir.resolve(UUID.randomUUID().toString());
        byte[] data = new byte[1000];
        new Random().nextBytes(data);
        Files.write(
                source,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        when(s3Client.putObject((PutObjectRequest) any(), (RequestBody) any())).thenReturn(putObjectResponse);
        when(putObjectResponse.eTag()).thenReturn("\"" + MD5.getMD5Hex(data) + "\"");

        Callable<Void> callable = new PutObjectCallable(
                "test-bucket",
                "test-key",
                "test-class",
                s3Client,
                source
        );
        callable.call();
    }

    @Test
    void shouldThrowException (@TempDir Path tempDir) throws Exception{
        Path source = tempDir.resolve(UUID.randomUUID().toString());
        byte[] data = new byte[1000];
        new Random().nextBytes(data);
        Files.write(
                source,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        when(s3Client.putObject((PutObjectRequest) any(), (RequestBody) any())).thenReturn(putObjectResponse);
        when(putObjectResponse.eTag()).thenReturn("wrong hash");

        Callable<Void> callable = new PutObjectCallable(
                "test-bucket",
                "test-key",
                "test-class",
                s3Client,
                source
        );

        Assertions.assertThrows(IncorrectHashException.class, callable::call);
    }
}
