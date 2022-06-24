package ru.rerumu.backups.factories;

import ru.rerumu.backups.services.ZFSFileWriter;

public interface ZFSFileWriterFactory {
    ZFSFileWriter getZFSFileWriter();
}
