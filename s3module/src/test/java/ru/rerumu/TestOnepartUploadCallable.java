package ru.rerumu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.factories.S3ClientFactory;
import ru.rerumu.s3.impl.OnepartUploadCallable;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.utils.MD5;
import ru.rerumu.utils.callables.CallableExecutor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestOnepartUploadCallable {

    @Mock
    S3ClientFactory s3ClientFactory;
    @Mock
    CallableExecutor callableExecutor;
    @Mock
    S3Client s3Client;
    @Mock
    PutObjectResponse putObjectResponse;

    @Test
    void shouldCall(@TempDir Path tempDir) throws Exception {
        Path target = tempDir.resolve(UUID.randomUUID().toString());
        S3Storage s3Storage = new S3Storage(
                Region.EU_NORTH_1,
                "bucket",
                "1111",
                "2222",
                Paths.get("prefix"),
                new URI("https://endpoint.example"),
                "standard"
        );
        byte[] data = new byte[1000];
        new Random().nextBytes(data);
        Files.write(
                target,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        when(s3ClientFactory.getS3Client(any())).thenReturn(s3Client);
        when(callableExecutor.callWithRetry(any())).thenReturn(putObjectResponse);
        when(putObjectResponse.eTag()).thenReturn(
                String.format("\"%s\"",MD5.getMD5Hex(data))
        );

        OnepartUploadCallable callable = new OnepartUploadCallable(
                target,
                "key",
                s3Storage,
                s3ClientFactory,
                callableExecutor
        );

        callable.call();
    }

    @Test
    void shouldThrowException(@TempDir Path tempDir) throws Exception {
        Path target = tempDir.resolve(UUID.randomUUID().toString());
        S3Storage s3Storage = new S3Storage(
                Region.EU_NORTH_1,
                "bucket",
                "1111",
                "2222",
                Paths.get("prefix"),
                new URI("https://endpoint.example"),
                "standard"
        );
        byte[] data = new byte[1000];
        new Random().nextBytes(data);
        Files.write(
                target,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        when(s3ClientFactory.getS3Client(any())).thenReturn(s3Client);
        when(callableExecutor.callWithRetry(any())).thenReturn(putObjectResponse);
        when(putObjectResponse.eTag()).thenReturn("Wrong hash");

        OnepartUploadCallable callable = new OnepartUploadCallable(
                target,
                "key",
                s3Storage,
                s3ClientFactory,
                callableExecutor
        );

        Assertions.assertThrows(IncorrectHashException.class, callable::call);
    }
}
