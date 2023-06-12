package ru.rerumu.zfs.consumers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.zfs_backup_s3.zfs.consumers.SnapshotListStdConsumer;
import ru.rerumu.zfs_backup_s3.zfs.models.Snapshot;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestSnapshotListStdConsumer {

    @Test
    void shouldCall(){
        List<Snapshot> snapshotList = new ArrayList<>();
        String str = """
                Test@tmp1
                Test@tmp2
                Test@tmp3
                """;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);


        SnapshotListStdConsumer snapshotListStdConsumer = new SnapshotListStdConsumer(snapshotList);
        snapshotListStdConsumer.accept(bufferedInputStream);


        List<Snapshot> expectedSnapshotList = new ArrayList<>();
        expectedSnapshotList.add(new Snapshot("Test@tmp1"));
        expectedSnapshotList.add(new Snapshot("Test@tmp2"));
        expectedSnapshotList.add(new Snapshot("Test@tmp3"));

        Assertions.assertEquals(expectedSnapshotList,snapshotList);
    }

    @Test
    void shouldThrowException() throws Exception{
        List<Snapshot> snapshotList = new ArrayList<>();
        BufferedInputStream bufferedInputStream = mock(BufferedInputStream.class);

        when(bufferedInputStream.readAllBytes()).thenThrow(IOException.class);


        SnapshotListStdConsumer snapshotListStdConsumer = new SnapshotListStdConsumer(snapshotList);
        Assertions.assertThrows(RuntimeException.class,()->snapshotListStdConsumer.accept(bufferedInputStream));

    }

    @Test
    void shouldThrowValidationException() throws Exception{
        List<Snapshot> snapshotList = new ArrayList<>();
        String str = """
                Some kind of error
                """;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream);


        SnapshotListStdConsumer snapshotListStdConsumer = new SnapshotListStdConsumer(snapshotList);
        Assertions.assertThrows(RuntimeException.class,()->snapshotListStdConsumer.accept(bufferedInputStream));

    }
}
