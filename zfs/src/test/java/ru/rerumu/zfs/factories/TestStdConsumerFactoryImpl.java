package ru.rerumu.zfs.factories;

import org.junit.jupiter.api.Test;
import ru.rerumu.zfs.factories.impl.StdConsumerFactoryImpl;
import ru.rerumu.zfs.models.Snapshot;

import java.util.ArrayList;
import java.util.List;

public class TestStdConsumerFactoryImpl {

    @Test
    void shouldCreate(){
        List<Snapshot> list =  new ArrayList<>();

        StdConsumerFactoryImpl factory =  new StdConsumerFactoryImpl();
        factory.getSnapshotListStdConsumer(list);
    }

    @Test
    void shouldCreate1(){
        List<String> list =  new ArrayList<>();

        StdConsumerFactoryImpl factory =  new StdConsumerFactoryImpl();
        factory.getDatasetStringStdConsumer(list);
    }
}
