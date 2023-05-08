package ru.rerumu.zfs.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TestPool {

    @Test
    void shouldCreate(){
        String name = "TestPool";
        List<Dataset> datasetList = new ArrayList<>();
        datasetList.add(new Dataset(
                "TestPool",
                List.of(
                        new Snapshot("TestPool@tmp1"),
                        new Snapshot("TestPool@tmp2"),
                        new Snapshot("TestPool@tmp3")
                )
        ));
        datasetList.add(new Dataset(
                "TestPool/encrypted",
                List.of(
                        new Snapshot("TestPool/encrypted@tmp1"),
                        new Snapshot("TestPool/encrypted@tmp2"),
                        new Snapshot("TestPool/encrypted@tmp3")
                )
        ));
        datasetList.add(new Dataset(
                "TestPool/encrypted/tested",
                List.of(
                        new Snapshot("TestPool/encrypted/tested@tmp1"),
                        new Snapshot("TestPool/encrypted/tested@tmp2"),
                        new Snapshot("TestPool/encrypted/tested@tmp3")
                )
        ));

        Pool pool =  new Pool(name,datasetList);


        Dataset expectedRootDataset = new Dataset(
                "TestPool",
                List.of(
                        new Snapshot("TestPool@tmp1"),
                        new Snapshot("TestPool@tmp2"),
                        new Snapshot("TestPool@tmp3")
                )
        );

        List<Dataset> expectedDatasetList = new ArrayList<>();
        expectedDatasetList.add(new Dataset(
                "TestPool",
                List.of(
                        new Snapshot("TestPool@tmp1"),
                        new Snapshot("TestPool@tmp2"),
                        new Snapshot("TestPool@tmp3")
                )
        ));
        expectedDatasetList.add(new Dataset(
                "TestPool/encrypted",
                List.of(
                        new Snapshot("TestPool/encrypted@tmp1"),
                        new Snapshot("TestPool/encrypted@tmp2"),
                        new Snapshot("TestPool/encrypted@tmp3")
                )
        ));
        expectedDatasetList.add(new Dataset(
                "TestPool/encrypted/tested",
                List.of(
                        new Snapshot("TestPool/encrypted/tested@tmp1"),
                        new Snapshot("TestPool/encrypted/tested@tmp2"),
                        new Snapshot("TestPool/encrypted/tested@tmp3")
                )
        ));


        Assertions.assertEquals("TestPool",pool.name());
        Assertions.assertEquals(expectedRootDataset,pool.getRootDataset().orElseThrow());
        Assertions.assertEquals(expectedDatasetList,pool.datasetList());
    }
}
