package ru.rerumu.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TestMD5 {

    MD5 md5 =  new MD5();

    @Test
    void test() throws Exception{
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] tmp = new byte[8192];
        new Random().nextBytes(tmp);
        md.update(tmp,0,0);
        byte[] hash = md.digest();

        MessageDigest md1 = MessageDigest.getInstance("MD5");
        byte[] hash1 = md1.digest();

        Assertions.assertArrayEquals(hash,hash1);
    }

    @Test
    void test1() throws Exception{

        byte[] src = new byte[8192];
        new Random().nextBytes(src);

        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(src);
        byte[] hash = md.digest();

        MessageDigest md1 = MessageDigest.getInstance("MD5");
        md1.update(src);
        byte[] tmp = new byte[8192];
        new Random().nextBytes(tmp);
        md1.update(tmp,0,0);
        byte[] hash1 = md1.digest();

        Assertions.assertArrayEquals(hash,hash1);
    }

    @Test
    void shouldGetMD5Hex()throws Exception{
        byte[] data =  new byte[1000];
        new Random().nextBytes(data);

        MD5.getMD5Hex(data);
    }

    @Test
    void shouldGetMD5Hex1(@TempDir Path tempDir )throws Exception{
        byte[] data =  new byte[1000];
        new Random().nextBytes(data);
        Path target = tempDir.resolve(UUID.randomUUID().toString());

        Files.write(
                target,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        MD5.getMD5Hex(target);
    }

    @Test
    void shouldGetMD5Bytes()throws Exception{
        byte[] data =  new byte[1000];
        new Random().nextBytes(data);

        MD5.getMD5Bytes(data);
    }

    @Test
    void shouldGetMD5Bytes1(@TempDir Path tempDir )throws Exception{
        byte[] data =  new byte[1000];
        new Random().nextBytes(data);
        Path target = tempDir.resolve(UUID.randomUUID().toString());

        Files.write(
                target,
                data,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );

        MD5.getMD5Bytes(target);
    }

}