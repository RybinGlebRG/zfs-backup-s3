package ru.rerumu.backups.services;

import ru.rerumu.backups.exceptions.IncorrectHashException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface UploadManager {
    void run() throws IOException, NoSuchAlgorithmException, IncorrectHashException;
}
