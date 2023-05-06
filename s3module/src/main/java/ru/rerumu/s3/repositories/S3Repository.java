package ru.rerumu.s3.repositories;

import ru.rerumu.s3.exceptions.IncorrectHashException;
import ru.rerumu.s3.exceptions.S3MissesFileException;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface S3Repository {

    void add(String prefix,Path path) ;

    List<String> listAll(String prefix);

    void getOne(String prefix, Path targetPath);

}
