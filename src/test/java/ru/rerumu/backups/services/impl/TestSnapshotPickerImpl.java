package ru.rerumu.backups.services.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.backups.exceptions.BaseSnapshotNotFoundException;
import ru.rerumu.backups.exceptions.SnapshotNotFoundException;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSFileSystem;
import ru.rerumu.backups.services.SnapshotPicker;
import ru.rerumu.backups.services.impl.SnapshotPickerImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestSnapshotPickerImpl {

    @Test
    void shouldPickBase() throws BaseSnapshotNotFoundException, SnapshotNotFoundException {
        ZFSFileSystem zfsFileSystem = new ZFSFileSystem(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                )
        );

        SnapshotPicker snapshotPicker = new SnapshotPickerImpl();
        List<Snapshot> snapshotList = snapshotPicker.pick(zfsFileSystem,"auto-20220326-150000");

        List<Snapshot> expectedSnapshotList = List.of(
                new Snapshot("ExternalPool@auto-20220326-150000")
        );

        Assertions.assertEquals(expectedSnapshotList,snapshotList);
    }

    @Test
    void shouldPickFew() throws BaseSnapshotNotFoundException, SnapshotNotFoundException {
        ZFSFileSystem zfsFileSystem = new ZFSFileSystem(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                )
        );

        SnapshotPicker snapshotPicker = new SnapshotPickerImpl();
        List<Snapshot> snapshotList = snapshotPicker.pick(zfsFileSystem,"auto-20220327-150000");

        List<Snapshot> expectedSnapshotList = List.of(
                new Snapshot("ExternalPool@auto-20220326-150000"),
                new Snapshot("ExternalPool@auto-20220327-060000"),
                new Snapshot("ExternalPool@auto-20220327-150000")
        );

        Assertions.assertEquals(expectedSnapshotList,snapshotList);
    }

    @Test
    void shouldNotPick() throws BaseSnapshotNotFoundException, SnapshotNotFoundException {
        ZFSFileSystem zfsFileSystem = new ZFSFileSystem(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                )
        );

        SnapshotPicker snapshotPicker = new SnapshotPickerImpl();

        Assertions.assertThrows(SnapshotNotFoundException.class,()->snapshotPicker.pick(zfsFileSystem,"test-20220328-150000"));

    }
}