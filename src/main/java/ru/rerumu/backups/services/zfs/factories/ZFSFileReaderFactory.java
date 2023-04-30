package ru.rerumu.backups.services.zfs.factories;

import ru.rerumu.backups.services.zfs.ZFSFileReader;

import java.io.BufferedOutputStream;
import java.nio.file.Path;

public interface ZFSFileReaderFactory {
    ZFSFileReader getZFSFileReader(BufferedOutputStream bufferedOutputStream, Path path);
}
