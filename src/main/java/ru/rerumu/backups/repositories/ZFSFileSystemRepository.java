package ru.rerumu.backups.repositories;

import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.models.ZFSPool;

import java.util.List;

public interface ZFSFileSystemRepository {
    List<ZFSFileSystem> getAllFilesystems(ZFSPool pool);
}
