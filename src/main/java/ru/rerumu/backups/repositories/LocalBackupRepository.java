package ru.rerumu.backups.repositories;

import ru.rerumu.backups.exceptions.*;
import ru.rerumu.backups.models.meta.BackupMeta;
import ru.rerumu.backups.models.meta.DatasetMeta;

import java.io.*;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface LocalBackupRepository {

//    BufferedInputStream getNextInputStream() throws NoMorePartsException, FinishedFlagException, IOException, TooManyPartsException;
//    Path getNextInputPath() throws NoMorePartsException, FinishedFlagException, IOException, TooManyPartsException;
//    void deleteLastPart() throws IOException;
    void delete(Path path) throws IOException;
//    void markReceivedLastPart() throws IOException;
    Path markReceived(Path path) throws IOException;
//    Path markReady(Path path) throws IOException;
//    BufferedOutputStream newPart()  throws IOException;
//    Path getLastPart();
//    boolean isExists(Path path);
    Path createNewFilePath(String prefix, int partNumber);
//    OutputStream createNewOutputStream(Path path) throws IOException;
//    InputStream createNewInputStream(Path path) throws IOException;

    Path getPart(String datasetName, String partName) throws IOException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException;
    Path getNextPart(String datasetName, String partName) throws IOException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException, FinishedFlagException;

    List<String> getDatasets() throws IOException, NoSuchAlgorithmException, IncorrectHashException;
    List<String> getParts(String datasetName) throws IOException, NoSuchAlgorithmException, IncorrectHashException;

    void add(String datasetName, String partName, Path path) throws IOException, S3MissesFileException, NoSuchAlgorithmException, IncorrectHashException, InterruptedException;
}
