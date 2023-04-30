package ru.rerumu.backups.services.zfs;

import ru.rerumu.backups.services.zfs.models.Dataset;
import ru.rerumu.backups.services.zfs.models.Pool;

public interface ZFSService {

    Pool getPool(String name);
    Dataset getDataset(String name);
}
