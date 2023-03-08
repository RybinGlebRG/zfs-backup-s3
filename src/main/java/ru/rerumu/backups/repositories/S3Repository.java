package ru.rerumu.backups.repositories;

import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.exceptions.S3MissesFileException;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface S3Repository {

    void add(String prefix,Path path) throws IOException, NoSuchAlgorithmException, IncorrectHashException, S3MissesFileException;

    List<String> listAll(String prefix);

    String getOne(String prefix);

}
