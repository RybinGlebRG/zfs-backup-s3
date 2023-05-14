package ru.rerumu.s3.operations;


import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.impl.operations.MultipartUploadCallable;
import ru.rerumu.s3.services.S3RequestService;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TestMultipartUploadCallable {

    @Mock
    S3RequestService s3RequestService;

    @Mock
    CreateMultipartUploadResponse createMultipartUploadResponse;

    @Mock
    UploadPartResponse uploadPartResponse;

    @Test
    void shouldUpload(@TempDir Path tempDir) throws Exception{
        Path target = tempDir.resolve(UUID.randomUUID().toString());

        byte[] data = new byte[2000];
        new Random().nextBytes(data);

        byte[] expected = ArrayUtils.clone(data);

        Files.write(
                target,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        when(s3RequestService.createMultipartUpload(anyString())).thenReturn(createMultipartUploadResponse);
        when(createMultipartUploadResponse.uploadId()).thenReturn("1234");

        when(s3RequestService.uploadPart(anyString(),anyString(),any(),any()))
                .thenReturn(uploadPartResponse);
        when(uploadPartResponse.eTag()).thenReturn("Wrong hash");


        MultipartUploadCallable callable = new MultipartUploadCallable(
                target,
                "test-key",
                100,
                s3RequestService
        );

        callable.call();
    }
}
