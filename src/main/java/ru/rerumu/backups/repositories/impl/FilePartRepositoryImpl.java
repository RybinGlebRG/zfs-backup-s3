package ru.rerumu.backups.repositories.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.Configuration;
import ru.rerumu.backups.exceptions.FinishedFlagException;
import ru.rerumu.backups.exceptions.NoMorePartsException;
import ru.rerumu.backups.exceptions.TooManyPartsException;
import ru.rerumu.backups.repositories.FilePartRepository;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilePartRepositoryImpl implements FilePartRepository {

    private static final String filePostfix = ".part";
    private static final String FINISH_MARK = "finished";
    private final String fileSuffix;
    private final Path backupDirectory;
    private final Logger logger = LoggerFactory.getLogger(FilePartRepositoryImpl.class);
    private Path lastPart;
    private int n = 0;

    public FilePartRepositoryImpl(Path backupDirectory,
                                  String fileSuffix){

        this.backupDirectory = backupDirectory;
        this.fileSuffix = fileSuffix;
    }

    public Path getLastPart() {
        return lastPart;
    }

    @Override
    public boolean isLastPartExists() {
        logger.info(String.format("Checking '%s' exists",lastPart.toString()));
        return Files.exists(lastPart);
    }

    public BufferedInputStream getNextInputStream() throws NoMorePartsException, FinishedFlagException, IOException, TooManyPartsException {
        List<Path> fileParts = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupDirectory)) {
            for (Path item : stream) {
                logger.info(String.format("Found file '%s'",item.toString()));
                if (item.toString().endsWith(".ready") || item.getFileName().toString().equals(FINISH_MARK)) {
                    logger.info(String.format("Accepted file '%s'",item.toString()));
                    fileParts.add(item);
                }
            }
        }

        if (fileParts.size() == 0){
            throw new NoMorePartsException();
        }
        else if (fileParts.size() > 1){
            throw new TooManyPartsException();
        }
        else {

            Path filePart = fileParts.get(0);
            if (filePart.getFileName().toString().equals(FINISH_MARK)) {
                throw new FinishedFlagException();
            }

            lastPart = filePart;
            InputStream inputStream = Files.newInputStream(filePart);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            return bufferedInputStream;
        }


    }

    public void deleteLastPart() throws IOException {
        Files.delete(lastPart);
    }

    @Override
    public void markReceivedLastPart() throws IOException {
        String fileName = lastPart.getFileName().toString();
        fileName = fileName.replace(".ready",".received");
        Path res = Paths.get(lastPart.getParent().toString(),fileName);
        Files.move(lastPart,res);
        lastPart=res;
    }

    @Override
    public void markReadyLastPart() throws IOException {
        String fileName = lastPart.getFileName().toString();
        fileName = fileName+".ready";
        Path res = Paths.get(lastPart.getParent().toString(),fileName);
        Files.move(lastPart,res);
        lastPart=res;
    }

    @Override
    public BufferedOutputStream newPart() throws IOException {
        Path filePart = Paths.get(backupDirectory.toString(), fileSuffix + filePostfix + n);
        lastPart = filePart;
        BufferedOutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(filePart));
        n++;
        return outputStream;
    }

}
