package ru.rerumu.utils;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

    private static final Logger logger = LoggerFactory.getLogger(MD5.class);

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
            byte[] buf = new byte[8192];
            int len;
            while((len= bufferedInputStream.read(buf))!=-1){
                md.update(buf,0,len);
            }
            String md5 = Hex.encodeHexString(md.digest());
            logger.info(String.format("Part hex MD5: '%s'", md5));
            return md5;

        }
    }
}
