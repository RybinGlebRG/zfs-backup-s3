package ru.rerumu.backups.services.impl;


import org.junit.jupiter.api.*;
import ru.rerumu.backups.models.CryptoMessage;
import ru.rerumu.backups.services.Cryptor;
import ru.rerumu.backups.services.impl.AESCryptor;

import java.io.*;
import java.util.Arrays;
import java.util.Random;

public class TestAESCryptor {


    @Test
    void encryptDecrypt1500() throws Exception {
        Cryptor cryptor = new AESCryptor("sduifysdrf");
        byte[] src = new byte[1500];
        new Random().nextBytes(src);

        CryptoMessage cryptoMessage = cryptor.encryptChunk(src);

        Cryptor cryptor2 = new AESCryptor("sduifysdrf");
        byte[] dst = cryptor2.decryptChunk(cryptoMessage);

        Assertions.assertArrayEquals(src, dst);
    }

    @Test
    void encryptDecrypt1000() throws Exception {
        Cryptor cryptor = new AESCryptor("sduifysdrf");
        byte[] src = new byte[1000];
        new Random().nextBytes(src);

        CryptoMessage cryptoMessage = cryptor.encryptChunk(src);

        Cryptor cryptor2 = new AESCryptor("sduifysdrf");
        byte[] dst = cryptor2.decryptChunk(cryptoMessage);

        Assertions.assertArrayEquals(src, dst);
    }

    @Test
    void encryptDecrypt1024() throws Exception {
        Cryptor cryptor = new AESCryptor("sduifysdrf");
        byte[] src = new byte[1024];
        new Random().nextBytes(src);

        CryptoMessage cryptoMessage = cryptor.encryptChunk(src);

        Cryptor cryptor2 = new AESCryptor("sduifysdrf");
        byte[] dst = cryptor2.decryptChunk(cryptoMessage);

        Assertions.assertArrayEquals(src, dst);
    }

    @Test
    void encryptDecrypt10() throws Exception {
        Cryptor cryptor = new AESCryptor("sduifysdrf");
        byte[] src = new byte[10];
        new Random().nextBytes(src);

        CryptoMessage cryptoMessage = cryptor.encryptChunk(src);

        Cryptor cryptor2 = new AESCryptor("sduifysdrf");
        byte[] dst = cryptor2.decryptChunk(cryptoMessage);

        Assertions.assertArrayEquals(src, dst);
    }

    @Test
    void encryptDecryptMultichunk() throws Exception {
        Cryptor cryptor = new AESCryptor("sduifysdrf");

        byte[] src = new byte[15];
        new Random().nextBytes(src);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(src);


        byte[] buf = new byte[10];
        int len;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            while ((len = byteArrayInputStream.read(buf)) >= 0) {
                byte[] tmp = Arrays.copyOfRange(buf, 0, len);
                CryptoMessage cryptoMessage = cryptor.encryptChunk(tmp);
                objectOutputStream.writeObject(cryptoMessage);
            }
        }

        ByteArrayInputStream byteArrayInputStream1 = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        Cryptor cryptor1 = new AESCryptor("sduifysdrf");

        ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream();
        try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream1)) {
            while (true) {
                Object object = objectInputStream.readObject();
                if (object instanceof CryptoMessage) {
                    CryptoMessage cryptoMessage = (CryptoMessage) object;
                    byte[] tmp = cryptor1.decryptChunk(cryptoMessage);
                    byteArrayOutputStream1.write(tmp);
                } else {
                    throw new IOException();
                }
            }

        } catch (EOFException e) {

        }

        Assertions.assertArrayEquals(src, byteArrayOutputStream1.toByteArray());
    }

}
