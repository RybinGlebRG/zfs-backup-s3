package ru.rerumu.backups.factories;

import ru.rerumu.backups.services.ZFSFileReader;

import java.io.BufferedOutputStream;
import java.nio.file.Path;

public interface ZFSFileReaderFactory {
    ZFSFileReader getZFSFileReader(BufferedOutputStream bufferedOutputStream, Path path);
}
