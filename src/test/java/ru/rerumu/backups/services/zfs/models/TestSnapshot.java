package ru.rerumu.backups.services.zfs.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.backups.services.zfs.models.Snapshot;

import java.util.Objects;

public class TestSnapshot {

    @Test
    void shouldCreate(){
        String lastFullSnapshotFullName = "MainPool@level_0_25_02_2020__20_50";
        Snapshot lastFullSnapshot = new Snapshot(lastFullSnapshotFullName);

        Assertions.assertEquals(lastFullSnapshot.getFullName(),"MainPool@level_0_25_02_2020__20_50");
        Assertions.assertEquals(lastFullSnapshot.getDataset(),"MainPool");
        Assertions.assertEquals(lastFullSnapshot.getName(),"level_0_25_02_2020__20_50");
    }

    @Test
    void shouldCreateSame(){
        Snapshot one = new Snapshot("MainPool@level_0_25_02_2020__20_50");
        Snapshot two = new Snapshot("MainPool","level_0_25_02_2020__20_50");

        Assertions.assertEquals(one,two);
    }

    @Test
    void shouldHaveSameHash(){
        Snapshot one = new Snapshot("MainPool@level_0_25_02_2020__20_50");
        int hash = Objects.hash(one.getName(),one.getDataset(),one.getFullName());

        Assertions.assertEquals(one.hashCode(),hash);
    }
}
