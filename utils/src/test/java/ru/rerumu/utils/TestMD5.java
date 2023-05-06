package ru.rerumu.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.MessageDigest;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TestMD5 {

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

}