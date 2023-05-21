package ru.rerumu.s3.services.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.services.impl.requests.UploadPartCallable;
import ru.rerumu.s3.services.impl.requests.models.UploadPartResult;
import ru.rerumu.utils.MD5;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.util.Random;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestUploadPartCallable {

    @Mock
    S3Client s3Client;

    @Mock
    UploadPartResponse uploadPartResponse;

    @Test
    void shouldCall() throws Exception {
        byte[] data = new byte[1000];
        new Random().nextBytes(data);

        when(s3Client.uploadPart((UploadPartRequest) any(), (RequestBody) any())).thenReturn(uploadPartResponse);
        when(uploadPartResponse.eTag()).thenReturn("\"" + MD5.getMD5Hex(data) + "\"");

        Callable<UploadPartResult> callable = new UploadPartCallable(
                "test-bucket",
                "test-key",
                s3Client,
                "test-upload",
                1,
                data
        );
        UploadPartResult result = callable.call();

        Assertions.assertArrayEquals(MD5.getMD5Bytes(data), result.md5());
        Assertions.assertEquals(
                CompletedPart.builder()
                        .partNumber(1)
                        .eTag("\"" + MD5.getMD5Hex(data) + "\"")
                        .build(),
                result.completedPart()
        );

    }

    @Test
    void shouldThrowException() throws Exception {
        byte[] data = new byte[1000];
        new Random().nextBytes(data);

        when(s3Client.uploadPart((UploadPartRequest) any(), (RequestBody) any())).thenReturn(uploadPartResponse);
        when(uploadPartResponse.eTag()).thenReturn("wrong hash");

        Callable<UploadPartResult> callable = new UploadPartCallable(
                "test-bucket",
                "test-key",
                s3Client,
                "test-upload",
                1,
                data
        );

        Assertions.assertThrows(IncorrectHashException.class, callable::call);

    }
}
