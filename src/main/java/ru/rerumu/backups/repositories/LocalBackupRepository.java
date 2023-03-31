package ru.rerumu.backups.repositories;

import ru.rerumu.backups.exceptions.*;

import java.io.*;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Deprecated
public interface LocalBackupRepository {

    Path getPart(String datasetName, String partName)
            throws IOException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException, NoPartFoundException;
    List<String> getDatasets() throws IOException, NoSuchAlgorithmException, IncorrectHashException;
    List<String> getParts(String datasetName) throws IOException, NoSuchAlgorithmException, IncorrectHashException;
    void add(String datasetName, String partName, Path path) throws IOException, S3MissesFileException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException;
}
