package ru.rerumu.backups.repositories;

import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.models.ZFSPool;

import java.io.IOException;
import java.util.List;

public interface ZFSFileSystemRepository {
//    List<ZFSFileSystem> getAllFilesystems(ZFSPool pool);
    List<ZFSFileSystem> getFilesystemsTreeList(ZFSFileSystem zfsFileSystem) throws IOException, InterruptedException;
}
