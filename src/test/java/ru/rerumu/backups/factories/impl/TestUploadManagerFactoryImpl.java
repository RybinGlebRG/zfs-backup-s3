package ru.rerumu.backups.factories.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.rerumu.backups.factories.UploadManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.UploadManager;
import ru.rerumu.backups.services.impl.MultipartUploadManager;
import ru.rerumu.backups.services.impl.OnepartUploadManager;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.BufferedInputStream;

import static org.junit.jupiter.api.Assertions.*;

class TestUploadManagerFactoryImpl {

    @Test
    void shouldGetOnepart() throws Exception {
        UploadManagerFactory uploadManagerFactory = new UploadManagerFactoryImpl(100_000);
        UploadManager uploadManager = uploadManagerFactory.getUploadManager(
                Mockito.mock(BufferedInputStream.class),
                100,
                Mockito.mock(S3Storage.class),
                "test",
                Mockito.mock(S3Client.class)
        );

        Assertions.assertInstanceOf(OnepartUploadManager.class,uploadManager);
    }

    @Test
    void shouldGetMultipart() throws Exception {
        UploadManagerFactory uploadManagerFactory = new UploadManagerFactoryImpl(100_000);
        UploadManager uploadManager = uploadManagerFactory.getUploadManager(
                Mockito.mock(BufferedInputStream.class),
                1_000_000,
                Mockito.mock(S3Storage.class),
                "test",
                Mockito.mock(S3Client.class)
        );

        Assertions.assertInstanceOf(MultipartUploadManager.class,uploadManager);
    }
}