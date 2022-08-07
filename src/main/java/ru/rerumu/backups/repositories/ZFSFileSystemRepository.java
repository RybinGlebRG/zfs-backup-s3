package ru.rerumu.backups.repositories;

import ru.rerumu.backups.models.ZFSDataset;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface ZFSFileSystemRepository {
    List<ZFSDataset> getFilesystemsTreeList(String fileSystemName) throws IOException, InterruptedException, ExecutionException;
}
