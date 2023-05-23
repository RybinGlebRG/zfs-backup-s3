package ru.rerumu.s3.services.requests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.factories.S3ClientFactory;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.s3.services.impl.requests.*;
import ru.rerumu.s3.services.impl.requests.models.UploadPartResult;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestCallableSupplierFactory {

    @Mock
    S3ClientFactory s3ClientFactory;

    @Mock
    S3Storage s3Storage;

    @Mock
    S3Client s3Client;

    @Test
    void shouldGetCreateMultipartUploadCallable()throws Exception{
        when(s3Storage.getBucketName()).thenReturn("test-bucket");
        when(s3Storage.getStorageClass()).thenReturn("test-class");
        when(s3ClientFactory.getS3Client(any())).thenReturn(s3Client);

        CallableSupplierFactory factory = new CallableSupplierFactory(s3ClientFactory,s3Storage);
        Callable<String> callable = factory.getCreateMultipartUploadSupplier("test-key").get();

        Assertions.assertTrue(callable instanceof CreateMultipartUploadCallable);
    }

    @Test
    void shouldGetUploadPartCallable()throws Exception{
        CallableSupplierFactory factory = new CallableSupplierFactory(s3ClientFactory,s3Storage);

        Callable<UploadPartResult> callable = factory.getUploadPartSupplier(
                "test-key",
                "test-upload",
                0,
                new byte[0]
        ).get();

        Assertions.assertTrue(callable instanceof UploadPartCallable);
    }

    @Test
    void shouldGetAbortMultipartUploadCallable()throws Exception{
        CallableSupplierFactory factory = new CallableSupplierFactory(s3ClientFactory,s3Storage);

        Callable<AbortMultipartUploadResponse> callable = factory.getAbortMultipartUploadSupplier(
                "test-key",
                "test-upload"
        ).get();

        Assertions.assertTrue(callable instanceof AbortMultipartUploadCallable);
    }

    @Test
    void shouldGetCompleteMultipartUploadCallable()throws Exception{
        CallableSupplierFactory factory = new CallableSupplierFactory(s3ClientFactory,s3Storage);

        Callable<Void> callable = factory.getCompleteMultipartUploadSupplier(
                new ArrayList<>(),
                "test-key",
                "test-upload",
                new ArrayList<>()
        ).get();

        Assertions.assertTrue(callable instanceof CompleteMultipartUploadCallable);
    }

    @Test
    void shouldGetListObjectCallable()throws Exception{
        CallableSupplierFactory factory = new CallableSupplierFactory(s3ClientFactory,s3Storage);

        Callable<ListObjectsResponse> callable = factory.getListObjectSupplier("test-key").get();

        Assertions.assertTrue(callable instanceof ListObjectCallable);
    }

    @Test
    void shouldGetListObjectCallable1()throws Exception{
        CallableSupplierFactory factory = new CallableSupplierFactory(s3ClientFactory,s3Storage);

        Callable<ListObjectsResponse> callable = factory.getListObjectSupplier(
                "test-key",
                "test-marker"
        ).get();

        Assertions.assertTrue(callable instanceof ListObjectCallable);
    }

    @Test
    void shouldGetPutObjectCallable()throws Exception{
        CallableSupplierFactory factory = new CallableSupplierFactory(s3ClientFactory,s3Storage);

        Callable<Void> callable = factory.getPutObjectSupplier(
                Paths.get("test"),
                "test-key"
        ).get();

        Assertions.assertTrue(callable instanceof PutObjectCallable);
    }

    @Test
    void shouldGetGetObjectRangedCallable()throws Exception{
        CallableSupplierFactory factory = new CallableSupplierFactory(s3ClientFactory,s3Storage);

        Callable<byte[]> callable = factory.getGetObjectRangedSupplier(
                "test-key",
                0L,
                1L,
                Paths.get("test")
        ).get();

        Assertions.assertTrue(callable instanceof GetObjectRangedCallable);
    }
}
