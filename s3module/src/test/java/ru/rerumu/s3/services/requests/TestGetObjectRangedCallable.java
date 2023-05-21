package ru.rerumu.s3.services.requests;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.services.impl.requests.GetObjectRangedCallable;
import ru.rerumu.utils.MD5;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestGetObjectRangedCallable {

    @Mock
    S3Client s3Client;

    @Mock
    ResponseBytes<GetObjectResponse> response;

    @Test
    void shouldCall(@TempDir Path tempDir) throws Exception{
        Path target = tempDir.resolve(UUID.randomUUID().toString());
        byte[] data = new byte[1000];
        new Random().nextBytes(data);

        when(s3Client.getObject((GetObjectRequest) any(), (ResponseTransformer<GetObjectResponse, Object>) any()))
                .thenReturn(response);
        when(response.asByteArray()).thenReturn(data);

        Callable<byte[]> callable = new GetObjectRangedCallable(
                "test-key",
                "test-bucket",
                0L,
                0L,
                s3Client,
                target
        );

        byte[] md5 = callable.call();

        byte[] res = Files.readAllBytes(target);

        Assertions.assertArrayEquals(MD5.getMD5Bytes(data),md5);
        Assertions.assertArrayEquals(data,res);

    }

}
