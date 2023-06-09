package ru.rerumu.s3;

import ru.rerumu.s3.models.S3Storage;

import java.nio.file.Path;
import java.util.UUID;

public interface S3ServiceFactory {

    S3Service getS3Service(
            S3Storage s3Storage,
            int maxPartSize,
            long filePartSize,
            Path tempDir,
            UUID uuid
    );
}
