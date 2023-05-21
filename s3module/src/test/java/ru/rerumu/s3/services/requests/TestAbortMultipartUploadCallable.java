package ru.rerumu.s3.services.requests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.services.impl.requests.AbortMultipartUploadCallable;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadResponse;

import java.util.concurrent.Callable;

@ExtendWith(MockitoExtension.class)
public class TestAbortMultipartUploadCallable {

    @Mock
    S3Client s3Client;

    @Test
    void shouldCall()throws Exception{
        Callable<AbortMultipartUploadResponse> callable = new AbortMultipartUploadCallable(
                "test-bucket",
                "test-key",
                s3Client,
                "test-upload"
        );
        callable.call();
    }
}
