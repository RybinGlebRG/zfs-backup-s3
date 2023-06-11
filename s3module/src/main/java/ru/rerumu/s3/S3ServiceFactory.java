package ru.rerumu.s3;

import java.nio.file.Path;
import java.util.UUID;
import ru.rerumu.s3.models.S3Storage;

public interface S3ServiceFactory {

    S3Service getS3Service(
            S3Storage s3Storage,
            int maxPartSize,
            long filePartSize,
            Path tempDir,
            UUID uuid
    );
}
