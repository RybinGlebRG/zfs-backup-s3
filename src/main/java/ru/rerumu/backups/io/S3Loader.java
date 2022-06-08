package ru.rerumu.backups.io;

import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.models.S3Storage;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface S3Loader {
    void addStorage(S3Storage s3Storage);
    void upload(String datasetName,Path path) throws IOException, InterruptedException, NoSuchAlgorithmException, IncorrectHashException;
    List<String> objectsList(String prefix);
}
