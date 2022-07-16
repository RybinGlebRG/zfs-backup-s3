package ru.rerumu.backups.factories.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.rerumu.backups.factories.S3ManagerFactory;
import ru.rerumu.backups.models.S3Storage;
import ru.rerumu.backups.services.S3Manager;
import ru.rerumu.backups.services.impl.MultipartUploadManager;
import ru.rerumu.backups.services.impl.OnepartUploadManager;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.BufferedInputStream;

class TestS3ManagerFactoryImpl {

    @Test
    void shouldGetOnepart() throws Exception {
        S3ManagerFactory uploadManagerFactory = new S3ManagerFactoryImpl(100_000);
        S3Manager s3Manager = uploadManagerFactory.getUploadManager(
                Mockito.mock(BufferedInputStream.class),
                100,
                Mockito.mock(S3Storage.class),
                "test",
                Mockito.mock(S3Client.class)
        );

        Assertions.assertInstanceOf(OnepartUploadManager.class, s3Manager);
    }

    @Test
    void shouldGetMultipart() throws Exception {
        S3ManagerFactory uploadManagerFactory = new S3ManagerFactoryImpl(100_000);
        S3Manager s3Manager = uploadManagerFactory.getUploadManager(
                Mockito.mock(BufferedInputStream.class),
                1_000_000,
                Mockito.mock(S3Storage.class),
                "test",
                Mockito.mock(S3Client.class)
        );

        Assertions.assertInstanceOf(MultipartUploadManager.class, s3Manager);
    }
}