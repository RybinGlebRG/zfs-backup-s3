package ru.rerumu.zfs.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

public class TestSnapshot {

    @Test
    void shouldCreate(){
        String lastFullSnapshotFullName = "MainPool@level_0_25_02_2020__20_50";
        Snapshot lastFullSnapshot = new Snapshot(lastFullSnapshotFullName);

        Assertions.assertEquals(lastFullSnapshot.getFullName(),"MainPool@level_0_25_02_2020__20_50");
        Assertions.assertEquals(lastFullSnapshot.getDataset(),"MainPool");
        Assertions.assertEquals(lastFullSnapshot.getName(),"level_0_25_02_2020__20_50");

        Assertions.assertEquals(lastFullSnapshot.dataset(),"MainPool");
        Assertions.assertEquals(lastFullSnapshot.name(),"level_0_25_02_2020__20_50");
    }

    @Test
    void shouldCreate2(){
        Snapshot lastFullSnapshot = new Snapshot("MainPool","level_0_25_02_2020__20_50");

        Assertions.assertEquals(lastFullSnapshot.getFullName(),"MainPool@level_0_25_02_2020__20_50");
        Assertions.assertEquals(lastFullSnapshot.getDataset(),"MainPool");
        Assertions.assertEquals(lastFullSnapshot.getName(),"level_0_25_02_2020__20_50");

        Assertions.assertEquals(lastFullSnapshot.dataset(),"MainPool");
        Assertions.assertEquals(lastFullSnapshot.name(),"level_0_25_02_2020__20_50");
    }

    @Test
    void shouldCreateSame(){
        Snapshot one = new Snapshot("MainPool@level_0_25_02_2020__20_50");
        Snapshot two = new Snapshot("MainPool","level_0_25_02_2020__20_50");

        Assertions.assertEquals(one,two);
    }
}
