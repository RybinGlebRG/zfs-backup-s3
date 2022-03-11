package ru.rerumu.backups.repositories;

import ru.rerumu.backups.exceptions.FinishedFlagException;
import ru.rerumu.backups.exceptions.NoMorePartsException;
import ru.rerumu.backups.exceptions.TooManyPartsException;

import java.io.*;
import java.nio.file.Path;

public interface FilePartRepository {

    BufferedInputStream getNextInputStream() throws NoMorePartsException, FinishedFlagException, IOException, TooManyPartsException;
    void deleteLastPart() throws IOException;
    void markReceivedLastPart() throws IOException;
    void markReadyLastPart() throws IOException;
    BufferedOutputStream newPart()  throws IOException;
    Path getLastPart();
    boolean isLastPartExists();
}
