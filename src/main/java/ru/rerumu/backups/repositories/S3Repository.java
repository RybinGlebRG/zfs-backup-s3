package ru.rerumu.backups.repositories;

import java.nio.file.Path;
import java.util.List;

public interface S3Repository {

    void add(String prefix,Path path);

    List<String> getAll(String prefix);

    String getOne(String prefix);

}
