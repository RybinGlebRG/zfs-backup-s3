package ru.rerumu.s3.operations;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.factories.S3ClientFactory;
import ru.rerumu.s3.impl.operations.OnepartUploadCallable;
import ru.rerumu.s3.models.S3Storage;
import ru.rerumu.s3.services.S3RequestService;
import ru.rerumu.utils.MD5;
import ru.rerumu.utils.callables.CallableExecutor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TestOnepartUploadCallable {

    @Mock
    S3RequestService s3RequestService;

    @Test
    void shouldCall(@TempDir Path tempDir) throws Exception {
        Path target = tempDir.resolve(UUID.randomUUID().toString());

        OnepartUploadCallable callable = new OnepartUploadCallable(
                target,
                "test-key",
                s3RequestService);
        callable.call();

        verify(s3RequestService).putObject(target,"test-key");

    }
}
