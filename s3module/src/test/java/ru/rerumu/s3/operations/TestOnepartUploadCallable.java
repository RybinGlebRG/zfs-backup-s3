package ru.rerumu.s3.operations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.s3.impl.operations.OnepartUploadCallable;
import ru.rerumu.s3.services.S3RequestService;

import java.nio.file.Path;
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
