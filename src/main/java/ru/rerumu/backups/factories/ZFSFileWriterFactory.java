package ru.rerumu.backups.factories;

import ru.rerumu.backups.services.ZFSFileWriter;

import java.io.IOException;
import java.nio.file.Path;

public interface ZFSFileWriterFactory {
    ZFSFileWriter getZFSFileWriter(Path path) throws IOException;
}
