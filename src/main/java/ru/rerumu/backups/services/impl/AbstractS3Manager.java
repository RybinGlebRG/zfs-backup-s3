package ru.rerumu.backups.services.impl;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.services.S3Manager;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractS3Manager implements S3Manager {
    private final Logger logger = LoggerFactory.getLogger(AbstractS3Manager.class);

    public abstract void run() throws IOException, NoSuchAlgorithmException, IncorrectHashException;

    protected String getMD5Hex(byte[] bytes)
            throws NoSuchAlgorithmException,
            IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream)) {

            String md5 = Hex.encodeHexString(md.digest(bufferedInputStream.readAllBytes()));
            logger.info(String.format("Part hex MD5: '%s'", md5));
            return md5;

        }
    }

    protected byte[] getMD5Bytes(byte[] bytes)
            throws NoSuchAlgorithmException,
            IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(byteArrayInputStream)) {
            return md.digest(bufferedInputStream.readAllBytes());
        }
    }

    protected byte[] getMD5Bytes(Path path)
            throws NoSuchAlgorithmException,
            IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream inputStream = Files.newInputStream(path) ;
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            return md.digest(bufferedInputStream.readAllBytes());
        }
    }

    protected String getMD5Hex(Path path)
            throws NoSuchAlgorithmException,
            IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream inputStream = Files.newInputStream(path) ;
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {

            String md5 = Hex.encodeHexString(md.digest(bufferedInputStream.readAllBytes()));
            logger.info(String.format("Part hex MD5: '%s'", md5));
            return md5;

        }
    }
}
