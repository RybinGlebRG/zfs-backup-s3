package ru.rerumu.zfs_backup_s3.utils;

import ru.rerumu.zfs_backup_s3.utils.impl.FileManagerImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@ThreadSafe
public sealed interface FileManager permits FileManagerImpl {

    Path getNew(String prefix, String postfix);

    void delete(Path path) throws IOException;

    List<Path> getPresentFiles() throws IOException;

    Path resolve(String str);
}
