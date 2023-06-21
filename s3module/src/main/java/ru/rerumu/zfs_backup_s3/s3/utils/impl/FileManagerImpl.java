package ru.rerumu.zfs_backup_s3.s3.utils.impl;

import ru.rerumu.zfs_backup_s3.s3.utils.FileManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// TODO: Check thread safe
public class FileManagerImpl implements FileManager {
    private final String unique;
    private final Path tempDir;

    public FileManagerImpl(String unique, Path tempDir) {
        this.unique = unique;
        this.tempDir = tempDir;
    }

    public Path getNew(String prefix, String postfix){
        StringBuilder stringBuilder = new StringBuilder();
        if (prefix != null){
            stringBuilder.append(prefix);
        }
        stringBuilder.append(unique);
        if (postfix != null){
            stringBuilder.append(postfix);
        }
        return tempDir.resolve(stringBuilder.toString());
    }

    public void delete(Path path) throws IOException {
        Files.delete(path);
    }
}
