package ru.rerumu.s3.factories;

import ru.rerumu.s3.ZFSFileWriter;

import java.io.IOException;
import java.nio.file.Path;

public interface ZFSFileWriterFactory {
    ZFSFileWriter getZFSFileWriter(Path path) throws IOException;
}
