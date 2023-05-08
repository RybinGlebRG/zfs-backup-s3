package ru.rerumu.zfs.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rerumu.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.zfs.factories.impl.ZFSCallableFactoryImpl;
import ru.rerumu.zfs.models.Dataset;
import ru.rerumu.zfs.models.Pool;
import ru.rerumu.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.util.ArrayList;
import java.util.function.Consumer;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class TestZFSCallableFactoryImpl {

    @Mock
    ProcessWrapperFactory processWrapperFactory;
    @Mock
    StdConsumerFactory stdConsumerFactory;

    @Test
    void shouldCreate() {
        ZFSCallableFactoryImpl factory = new ZFSCallableFactoryImpl(processWrapperFactory,stdConsumerFactory);
        factory.getPoolCallable("TestPool");
    }

    @Test
    void shouldCreate1() {
        ZFSCallableFactoryImpl factory = new ZFSCallableFactoryImpl(processWrapperFactory,stdConsumerFactory);
        factory.getDatasetCallable("TestDataset");
    }

    @Test
    void shouldCreate2() {
        Dataset dataset = new Dataset("TestDataset", new ArrayList<>());

        ZFSCallableFactoryImpl factory = new ZFSCallableFactoryImpl(processWrapperFactory,stdConsumerFactory);
        factory.getCreateSnapshotCallable(dataset,"tmp1",true);
    }

    @Test
    void shouldCreate3() {
        Dataset dataset = new Dataset("TestDataset", new ArrayList<>());

        ZFSCallableFactoryImpl factory = new ZFSCallableFactoryImpl(processWrapperFactory,stdConsumerFactory);
        factory.getListSnapshotsCallable(dataset);
    }

    @Test
    void shouldCreate4() {
        ZFSCallableFactoryImpl factory = new ZFSCallableFactoryImpl(processWrapperFactory,stdConsumerFactory);
        factory.getSendReplica(new Snapshot("TestDataset@tmp1"), (Consumer<BufferedInputStream>) mock(Consumer.class));
    }

    @Test
    void shouldCreate5() {
        Pool pool =  new Pool("TestPool",new ArrayList<>());

        ZFSCallableFactoryImpl factory = new ZFSCallableFactoryImpl(processWrapperFactory,stdConsumerFactory);
        factory.getReceive(pool, (Consumer<BufferedOutputStream>) mock(Consumer.class));
    }
}
