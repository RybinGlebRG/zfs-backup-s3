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
    private final Path backupDirectory;
    private final Logger logger = LoggerFactory.getLogger(FilePartRepositoryImpl.class);


    public FilePartRepositoryImpl(Path backupDirectory) {

        this.backupDirectory = backupDirectory;
    }

    @Override
    public Path createNewFilePath(String prefix, int partNumber) {
        Path filePart = Paths.get(backupDirectory.toString(), prefix + filePostfix + partNumber);
        return filePart;
    }

    @Override
    public void delete(Path path) throws IOException {
        Files.delete(path);
    }

    @Override
    public Path markReady(Path path) throws IOException {
        Path res = Paths.get(path.toString() + ".ready");
        Files.move(path, res);
        return res;
    }

    @Override
    public Path markReceived(Path path) throws IOException {
        String fileName = path.getFileName().toString();
        fileName = fileName.replace(".ready", ".received");
        Path res = Paths.get(path.getParent().toString(), fileName);
        Files.move(path, res);
        return res;
    }

    @Override
    public Path getNextInputPath() throws NoMorePartsException, FinishedFlagException, IOException, TooManyPartsException {
        List<Path> fileParts = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupDirectory)) {
            for (Path item : stream) {
                logger.info(String.format("Found file '%s'", item.toString()));
                if (item.toString().endsWith(".ready") || item.getFileName().toString().equals(FINISH_MARK)) {
                    logger.info(String.format("Accepted file '%s'", item.toString()));
                    fileParts.add(item);
                }
            }
        }

        if (fileParts.size() == 0) {
            throw new NoMorePartsException();
        } else if (fileParts.size() > 1) {
            throw new TooManyPartsException();
        } else {

            Path filePart = fileParts.get(0);
            if (filePart.getFileName().toString().equals(FINISH_MARK)) {
                throw new FinishedFlagException();
            }

            return filePart;

        }
    }
}
