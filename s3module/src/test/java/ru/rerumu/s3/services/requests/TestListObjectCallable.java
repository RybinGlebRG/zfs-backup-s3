package ru.rerumu.s3.services.requests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.services.impl.requests.ListObjectCallable;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;

import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestListObjectCallable {

    @Mock
    S3Client s3Client;

    @Test
    void shouldCallWithoutMarker() throws Exception{

        Callable<ListObjectsResponse> callable = new ListObjectCallable(
                "test-bucket",
                "test-key",
                s3Client,
                null
        );

        callable.call();
    }

    @Test
    void shouldCallWithMarker() throws Exception{

        Callable<ListObjectsResponse> callable = new ListObjectCallable(
                "test-bucket",
                "test-key",
                s3Client,
                "test-marker"
        );

        callable.call();
    }
}
