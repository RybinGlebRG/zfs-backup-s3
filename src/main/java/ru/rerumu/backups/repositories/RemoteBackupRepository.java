package ru.rerumu.backups.repositories;

import ru.rerumu.backups.exceptions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public interface RemoteBackupRepository {
    boolean isFileExists(String datasetName, String filename) throws IOException, NoSuchAlgorithmException, IncorrectHashException;
    void add(String prefix, Path path) throws IOException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException;
    Path getPart(String datasetName, String partName,Path targetDir) throws IOException, NoSuchAlgorithmException, IncorrectHashException, NoPartFoundException;

    Path getBackupMeta(Path targetDir) throws IOException, NoSuchAlgorithmException, IncorrectHashException, NoBackupMetaException;
    Path getDatasetMeta(String datasetName, Path targetDir) throws IOException, NoSuchAlgorithmException, IncorrectHashException, NoDatasetMetaException;
}
