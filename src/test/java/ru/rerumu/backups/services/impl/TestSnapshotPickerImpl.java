package ru.rerumu.backups.services.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.backups.exceptions.BaseSnapshotNotFoundException;
import ru.rerumu.backups.exceptions.SnapshotNotFoundException;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.models.zfs_dataset_properties.EncryptionProperty;
import ru.rerumu.backups.services.SnapshotPicker;

import java.util.List;

class TestSnapshotPickerImpl {

    @Test
    void shouldPickBase() throws BaseSnapshotNotFoundException, SnapshotNotFoundException {
        ZFSDataset zfsDataset = new ZFSDataset(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                ),
                EncryptionProperty.ON
        );

        SnapshotPicker snapshotPicker = new SnapshotPickerImpl();
        List<Snapshot> snapshotList = snapshotPicker.pick(zfsDataset,"auto-20220326-150000");

        List<Snapshot> expectedSnapshotList = List.of(
                new Snapshot("ExternalPool@auto-20220326-150000")
        );

        Assertions.assertEquals(expectedSnapshotList,snapshotList);
    }

    @Test
    void shouldPickFew() throws BaseSnapshotNotFoundException, SnapshotNotFoundException {
        ZFSDataset zfsDataset = new ZFSDataset(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                ),
                EncryptionProperty.ON
        );

        SnapshotPicker snapshotPicker = new SnapshotPickerImpl();
        List<Snapshot> snapshotList = snapshotPicker.pick(zfsDataset,"auto-20220327-150000");

        List<Snapshot> expectedSnapshotList = List.of(
                new Snapshot("ExternalPool@auto-20220326-150000"),
                new Snapshot("ExternalPool@auto-20220327-060000"),
                new Snapshot("ExternalPool@auto-20220327-150000")
        );

        Assertions.assertEquals(expectedSnapshotList,snapshotList);
    }

    @Test
    void shouldNotPick() throws BaseSnapshotNotFoundException, SnapshotNotFoundException {
        ZFSDataset zfsDataset = new ZFSDataset(
                "ExternalPool",
                List.of(
                        new Snapshot("ExternalPool@auto-20220326-150000"),
                        new Snapshot("ExternalPool@auto-20220327-060000"),
                        new Snapshot("ExternalPool@auto-20220327-150000"),
                        new Snapshot("ExternalPool@auto-20220328-150000")
                ),
                EncryptionProperty.ON
        );

        SnapshotPicker snapshotPicker = new SnapshotPickerImpl();

        Assertions.assertThrows(SnapshotNotFoundException.class,()->snapshotPicker.pick(zfsDataset,"test-20220328-150000"));

    }
}