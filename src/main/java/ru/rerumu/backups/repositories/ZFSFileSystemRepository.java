package ru.rerumu.backups.repositories;

import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.models.ZFSPool;

import java.io.IOException;
import java.util.List;

public interface ZFSFileSystemRepository {
    List<ZFSFileSystem> getFilesystemsTreeList(String fileSystemName) throws IOException, InterruptedException;
}
