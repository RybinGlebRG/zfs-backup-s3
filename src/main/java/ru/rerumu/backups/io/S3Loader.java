package ru.rerumu.backups.io;

import ru.rerumu.backups.models.S3Storage;

import java.io.IOException;
import java.nio.file.Path;

public interface S3Loader {
    void addStorage(S3Storage s3Storage);
    void upload(String datasetName,Path path) throws IOException, InterruptedException;
}
