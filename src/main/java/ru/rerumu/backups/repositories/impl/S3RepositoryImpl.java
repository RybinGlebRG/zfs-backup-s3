package ru.rerumu.backups.repositories.impl;

import ru.rerumu.backups.repositories.S3Repository;

import java.nio.file.Path;
import java.util.List;

public class S3RepositoryImpl implements S3Repository {
    @Override
    public void add(String prefix, Path path) {

    }

    @Override
    public List<String> getAll(String prefix) {
        return null;
    }

    @Override
    public String getOne(String prefix) {
        return null;
    }
}
