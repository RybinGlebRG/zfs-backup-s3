package ru.rerumu.zfs_backup_s3.utils;

import ru.rerumu.zfs_backup_s3.utils.impl.FileManagerImpl;

import java.io.IOException;
import java.nio.file.Path;

@ThreadSafe
public sealed interface FileManager permits FileManagerImpl {

    Path getNew(String prefix, String postfix);

    void delete(Path path) throws IOException;
}
