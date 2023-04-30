package ru.rerumu.backups.services.zfs.factories;

import ru.rerumu.backups.services.zfs.ZFSFileWriter;

import java.io.IOException;
import java.nio.file.Path;

public interface ZFSFileWriterFactory {
    ZFSFileWriter getZFSFileWriter(Path path) throws IOException;
}
