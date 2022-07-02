package ru.rerumu.backups.services.impl;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.services.UploadManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractUploadManager implements UploadManager {
    private final Logger logger = LoggerFactory.getLogger(AbstractUploadManager.class);

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
}
