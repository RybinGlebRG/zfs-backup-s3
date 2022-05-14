package ru.rerumu.backups.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestZFSStreamPart {

    @Test
    void shouldParse1(){
        Path path = Paths.get("/tmp/MainPool@level-0-09052022__MainPool@level-1-09052022.part0.ready");
        ZFSStreamPart zfsStreamPart = new ZFSStreamPart(path);

        Assertions.assertEquals(zfsStreamPart.getStreamName(),"MainPool@level-0-09052022__MainPool@level-1-09052022");
        Assertions.assertEquals(zfsStreamPart.getPartNumber(),0);
        Assertions.assertEquals(zfsStreamPart.getFilename().toString(),"MainPool@level-0-09052022__MainPool@level-1-09052022.part0.ready");
    }

    @Test
    void shouldParse2(){
        Path path = Paths.get("/tmp/MainPool@level-0-09052022__MainPool@level-1-09052022.part0");
        ZFSStreamPart zfsStreamPart = new ZFSStreamPart(path);

        Assertions.assertEquals(zfsStreamPart.getStreamName(),"MainPool@level-0-09052022__MainPool@level-1-09052022");
        Assertions.assertEquals(zfsStreamPart.getPartNumber(),0);
        Assertions.assertEquals(zfsStreamPart.getFilename().toString(),"MainPool@level-0-09052022__MainPool@level-1-09052022.part0");
    }

    @Test
    void shouldParse3(){
        Path path = Paths.get("/tmp/MainPool@level-0-09052022__MainPool@level-1-09052022.part0.received");
        ZFSStreamPart zfsStreamPart = new ZFSStreamPart(path);

        Assertions.assertEquals(zfsStreamPart.getStreamName(),"MainPool@level-0-09052022__MainPool@level-1-09052022");
        Assertions.assertEquals(zfsStreamPart.getPartNumber(),0);
        Assertions.assertEquals(zfsStreamPart.getFilename().toString(),"MainPool@level-0-09052022__MainPool@level-1-09052022.part0.received");
    }


}
