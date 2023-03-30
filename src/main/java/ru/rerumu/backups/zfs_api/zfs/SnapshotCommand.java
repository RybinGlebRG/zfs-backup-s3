package ru.rerumu.backups.zfs_api.zfs;

import java.util.concurrent.Callable;

public interface SnapshotCommand {

    void execute();
}
