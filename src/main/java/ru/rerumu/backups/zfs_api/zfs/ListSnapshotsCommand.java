package ru.rerumu.backups.zfs_api.zfs;

import ru.rerumu.backups.models.Snapshot;

import java.util.List;

@Deprecated
public interface ListSnapshotsCommand {

    List<Snapshot> execute();
}
