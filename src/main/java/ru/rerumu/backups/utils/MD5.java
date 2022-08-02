package ru.rerumu.backups.utils;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.impl.AbstractS3Manager;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

//    private final Logger logger = LoggerFactory.getLogger(AbstractS3Manager.class);

    public static String getMD5Hex(byte[] bytes)
            throws NoSuchAlgorithmException,
            IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream)) {

            String md5 = Hex.encodeHexString(md.digest(bufferedInputStream.readAllBytes()));
//            logger.info(String.format("Part hex MD5: '%s'", md5));
            return md5;

        }
    }

    public static byte[] getMD5Bytes(byte[] bytes)
            throws NoSuchAlgorithmException,
            IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream)) {
            return md.digest(bufferedInputStream.readAllBytes());
        }
    }

    public static byte[] getMD5Bytes(Path path)
            throws NoSuchAlgorithmException,
            IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream inputStream = Files.newInputStream(path);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            return md.digest(bufferedInputStream.readAllBytes());
        }
    }

    public static String getMD5Hex(Path path)
            throws NoSuchAlgorithmException,
            IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream inputStream = Files.newInputStream(path) ;
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            // TODO: file can be too big
            String md5 = Hex.encodeHexString(md.digest(bufferedInputStream.readAllBytes()));
//            logger.info(String.format("Part hex MD5: '%s'", md5));
            return md5;

        }
    }
}
