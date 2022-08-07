package ru.rerumu.backups.services;

import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.exceptions.S3MissesFileException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface S3Manager {
    void run() throws IOException, NoSuchAlgorithmException, IncorrectHashException;
}
