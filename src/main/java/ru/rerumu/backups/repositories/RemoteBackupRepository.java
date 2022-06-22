package ru.rerumu.backups.repositories;

import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.exceptions.S3MissesFileException;
import ru.rerumu.backups.models.S3Storage;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface RemoteBackupRepository {
//    void addStorage(S3Storage s3Storage);
//    void upload(String datasetName,Path path) throws IOException, InterruptedException, NoSuchAlgorithmException, IncorrectHashException;
//    List<String> objectsListForDataset(String datasetName);
    boolean isFileExists(String datasetName, String filename);
    void add(String datasetName, Path path) throws IOException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException;
}
