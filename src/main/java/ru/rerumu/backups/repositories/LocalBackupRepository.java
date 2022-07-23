package ru.rerumu.backups.repositories;

import ru.rerumu.backups.exceptions.*;

import java.io.*;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface LocalBackupRepository {

    void delete(Path path) throws IOException;
    void clear(String datasetName, String partName) throws IOException;
    Path getPart(String datasetName, String partName) throws IOException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException;
//    @Deprecated
//    Path getNextPart(String datasetName, String partName) throws IOException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException, FinishedFlagException, NoMorePartsException;

    List<String> getDatasets() throws IOException, NoSuchAlgorithmException, IncorrectHashException;
    List<String> getParts(String datasetName) throws IOException, NoSuchAlgorithmException, IncorrectHashException;
    void add(String datasetName, String partName, Path path) throws IOException, S3MissesFileException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException;
}
