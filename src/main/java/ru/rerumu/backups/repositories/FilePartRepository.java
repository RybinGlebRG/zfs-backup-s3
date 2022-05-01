package ru.rerumu.backups.repositories;

import ru.rerumu.backups.exceptions.FinishedFlagException;
import ru.rerumu.backups.exceptions.NoMorePartsException;
import ru.rerumu.backups.exceptions.TooManyPartsException;

import java.io.*;
import java.nio.file.Path;

public interface FilePartRepository {

//    BufferedInputStream getNextInputStream() throws NoMorePartsException, FinishedFlagException, IOException, TooManyPartsException;
    Path getNextInputPath() throws NoMorePartsException, FinishedFlagException, IOException, TooManyPartsException;
    void deleteLastPart() throws IOException;
    void delete(Path path) throws IOException;
    void markReceivedLastPart() throws IOException;
    Path markReceived(Path path) throws IOException;
    Path markReady(Path path) throws IOException;
//    BufferedOutputStream newPart()  throws IOException;
    Path getLastPart();
    boolean isExists(Path path);
    Path createNewFilePath(String template, int partNumber);
    OutputStream createNewOutputStream(Path path) throws IOException;
    InputStream createNewInputStream(Path path) throws IOException;

}
