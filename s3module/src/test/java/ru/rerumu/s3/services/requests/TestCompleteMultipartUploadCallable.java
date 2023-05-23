package ru.rerumu.s3.services.requests;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.services.impl.requests.CompleteMultipartUploadCallable;
import ru.rerumu.utils.MD5;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestCompleteMultipartUploadCallable {

    @Mock
    S3Client s3Client;

    @Mock
    CompleteMultipartUploadResponse response;

    @Test
    void shouldCall() throws Exception{
        byte[] bytes1 = new byte[1000];
        byte[] bytes2 = new byte[1000];
        byte[] bytes3 = new byte[300];

        new Random().nextBytes(bytes1);
        new Random().nextBytes(bytes2);
        new Random().nextBytes(bytes3);

        List<byte[]> md5List = new ArrayList<>();
        md5List.add(bytes1);
        md5List.add(bytes2);
        md5List.add(bytes3);

        byte[] concatenatedMd5 = md5List.stream()
                .reduce(new byte[0], ArrayUtils::addAll,ArrayUtils::addAll);
        String md5 = MD5.getMD5Hex(concatenatedMd5) + "-" + md5List.size();

        when(s3Client.completeMultipartUpload((CompleteMultipartUploadRequest) any())).thenReturn(response);
        when(response.eTag()).thenReturn(String.format("\"%s\"",md5));

        Callable<Void> callable = new CompleteMultipartUploadCallable(
                new ArrayList<>(),
                "test-bucket",
                "test-key",
                "test-upload",
                s3Client,
                md5List
        );
        callable.call();


    }

    @Test
    void shouldThrowException() throws Exception{
        byte[] bytes1 = new byte[1000];
        byte[] bytes2 = new byte[1000];
        byte[] bytes3 = new byte[300];

        new Random().nextBytes(bytes1);
        new Random().nextBytes(bytes2);
        new Random().nextBytes(bytes3);

        List<byte[]> md5List = new ArrayList<>();
        md5List.add(bytes1);
        md5List.add(bytes2);
        md5List.add(bytes3);

        byte[] concatenatedMd5 = md5List.stream()
                .reduce(new byte[0], ArrayUtils::addAll,ArrayUtils::addAll);
        String md5 = MD5.getMD5Hex(concatenatedMd5) + "-" + md5List.size();

        when(s3Client.completeMultipartUpload((CompleteMultipartUploadRequest) any())).thenReturn(response);
        when(response.eTag()).thenReturn("wrong hash");

        Callable<Void> callable = new CompleteMultipartUploadCallable(
                new ArrayList<>(),
                "test-bucket",
                "test-key",
                "test-upload",
                s3Client,
                md5List
        );

        Assertions.assertThrows(IncorrectHashException.class, callable::call);
    }
}
