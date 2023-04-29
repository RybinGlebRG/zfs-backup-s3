package ru.rerumu.backups.services.zfs;

import ru.rerumu.backups.models.zfs.Pool;

public interface ZFSService {

    Pool getPool(String name);
}
