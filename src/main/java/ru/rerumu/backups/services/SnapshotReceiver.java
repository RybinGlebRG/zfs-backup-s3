package ru.rerumu.backups.services;

import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.exceptions.IncorrectFilePartNameException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public interface SnapshotReceiver {
    void receiveSnapshotPart(Path path) throws IncorrectFilePartNameException, CompressorException, IOException, ClassNotFoundException, EncryptException, InterruptedException, ExecutionException;
    void finish() throws IOException, InterruptedException, ExecutionException;
}
