package ru.rerumu.backups.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.backups.exceptions.IncorrectFilePartNameException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestZFSStreamPart {

    @Test
    void shouldParse1() throws IncorrectFilePartNameException {
        Path path = Paths.get("/tmp/MainPool@level-0-09052022__MainPool@level-1-09052022.part0.ready");
        ZFSStreamPart zfsStreamPart = new ZFSStreamPart(path);

        Assertions.assertEquals(zfsStreamPart.getStreamName(),"MainPool@level-0-09052022__MainPool@level-1-09052022");
        Assertions.assertEquals(zfsStreamPart.getPartNumber(),0);
        Assertions.assertEquals(zfsStreamPart.getFilename().toString(),"MainPool@level-0-09052022__MainPool@level-1-09052022.part0.ready");
    }

    @Test
    void shouldParse2() throws IncorrectFilePartNameException {
        Path path = Paths.get("/tmp/MainPool@level-0-09052022__MainPool@level-1-09052022.part0");
        ZFSStreamPart zfsStreamPart = new ZFSStreamPart(path);

        Assertions.assertEquals(zfsStreamPart.getStreamName(),"MainPool@level-0-09052022__MainPool@level-1-09052022");
        Assertions.assertEquals(zfsStreamPart.getPartNumber(),0);
        Assertions.assertEquals(zfsStreamPart.getFilename().toString(),"MainPool@level-0-09052022__MainPool@level-1-09052022.part0");
    }

    @Test
    void shouldParse3() throws IncorrectFilePartNameException {
        Path path = Paths.get("/tmp/MainPool@level-0-09052022__MainPool@level-1-09052022.part0.received");
        ZFSStreamPart zfsStreamPart = new ZFSStreamPart(path);

        Assertions.assertEquals(zfsStreamPart.getStreamName(),"MainPool@level-0-09052022__MainPool@level-1-09052022");
        Assertions.assertEquals(zfsStreamPart.getPartNumber(),0);
        Assertions.assertEquals(zfsStreamPart.getFilename().toString(),"MainPool@level-0-09052022__MainPool@level-1-09052022.part0.received");
    }

    @Test
    void shouldParse4() throws IncorrectFilePartNameException {
        Path path = Paths.get("/tmp/MainPool-VMs@tmp_14.02.2022_23.05.part1.ready");
        ZFSStreamPart zfsStreamPart = new ZFSStreamPart(path);

        Assertions.assertEquals("MainPool-VMs@tmp_14.02.2022_23.05",zfsStreamPart.getStreamName());
        Assertions.assertEquals(1,zfsStreamPart.getPartNumber());
        Assertions.assertEquals("MainPool-VMs@tmp_14.02.2022_23.05.part1.ready",zfsStreamPart.getFilename().toString());
    }


    @Test
    void shouldNotParse() throws IncorrectFilePartNameException {
        Path path = Paths.get("/tmp/MainPool-VMs@tmp_14.02.2022_23.05");
        Assertions.assertThrows(IncorrectFilePartNameException.class,()->new ZFSStreamPart(path));

    }

    @Test
    void shouldGetFullPath() throws IncorrectFilePartNameException {
        Path path = Paths.get("/tmp/MainPool-VMs@tmp_14.02.2022_23.05.part1");
        ZFSStreamPart zfsStreamPart = new ZFSStreamPart(path);
        Assertions.assertEquals(path,zfsStreamPart.getFullPath());
    }
}
