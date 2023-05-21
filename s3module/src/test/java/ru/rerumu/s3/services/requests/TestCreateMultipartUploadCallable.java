package ru.rerumu.s3.services.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.services.impl.requests.CreateMultipartUploadCallable;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;

import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestCreateMultipartUploadCallable {

    @Mock
    S3Client s3Client;

    @Mock
    CreateMultipartUploadResponse response;

    @Test
    void shouldCall()throws Exception{

        when(s3Client.createMultipartUpload((CreateMultipartUploadRequest) any())).thenReturn(response);
        when(response.uploadId()).thenReturn("12345");

        Callable<String> callable = new CreateMultipartUploadCallable(
                "test-bucket",
                "test-key",
                "test-class",
                s3Client
        );
        String res = callable.call();

        Assertions.assertEquals("12345",res);
    }
}
