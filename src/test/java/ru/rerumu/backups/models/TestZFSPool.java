package ru.rerumu.backups.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestZFSPool {

    @Test
    void shouldGetName(){
        ZFSPool zfsPool = new ZFSPool("tank");

        Assertions.assertEquals("tank",zfsPool.getName());
    }

}