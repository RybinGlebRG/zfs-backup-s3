package ru.rerumu.backups.services.impl;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.Configuration;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.models.CryptoMessage;
import ru.rerumu.backups.services.Cryptor;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

@Deprecated
public class AESCryptor implements Cryptor {
    private static final Integer SALT_SIZE=8;

    private final Logger logger = LoggerFactory.getLogger(AESCryptor.class);
    private final String password;

    public AESCryptor(String password) {
        Security.setProperty("crypto.policy", "unlimited");
        Security.addProvider(new BouncyCastleProvider());
        this.password = password;
    }

    private SecretKey generateSecretKey(byte[] salt)
            throws IOException,
            NoSuchAlgorithmException,
            InvalidKeySpecException {
        KeySpec keySpec = new PBEKeySpec(this.password.toCharArray(), salt, 100000, 256);
        return new SecretKeySpec(
                SecretKeyFactory
                        .getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(keySpec)
                        .getEncoded()
                , "AES");
    }

    private IvParameterSpec generateIv(int blockSize) {
        byte[] byteiv = new byte[blockSize];
        new SecureRandom().nextBytes(byteiv);
        return new IvParameterSpec(byteiv);
    }

//    private byte[] getPrefix(InputStream inputStream, Integer ivBlockSize) throws IOException, EncryptException {
//        byte[] prefix = new byte[SALT_SIZE+ivBlockSize];
//
//        if (inputStream.read(prefix)<prefix.length){
//            throw new EncryptException("Wrong prefix");
//        }
//        return prefix;
//    }

//    private byte[] getSalt(byte[] prefix){
//        return Arrays.copyOfRange(prefix,0,SALT_SIZE);
//    }

//    private IvParameterSpec getIV(byte[] prefix){
//        byte[] iv = Arrays.copyOfRange(prefix,SALT_SIZE,prefix.length);
//        return new IvParameterSpec(iv);
//    }

    @Override
    public CryptoMessage encryptChunk(byte[] chunk) throws EncryptException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
            IvParameterSpec ivParameterSpec = generateIv(cipher.getBlockSize());

            byte[] salt = new byte[SALT_SIZE];
            new SecureRandom().nextBytes(salt);
            SecretKey secretKey = generateSecretKey(salt);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (InputStream inputStream = new ByteArrayInputStream(chunk);
                 CipherOutputStream cipherOutputStream = new CipherOutputStream(byteArrayOutputStream, cipher)) {

                byte[] buf = new byte[1024];
                int len;
                while ((len = inputStream.read(buf)) >= 0) {
                    cipherOutputStream.write(buf, 0, len);
                }
            }

            byte[] res = byteArrayOutputStream.toByteArray();
            CryptoMessage cryptoMessage = new CryptoMessage(salt,ivParameterSpec.getIV(),res);
            return cryptoMessage;

        } catch (Exception e) {
            throw new EncryptException(e);
        }
    }

    @Override
    public byte[] decryptChunk(CryptoMessage cryptoMessage) throws EncryptException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (InputStream inputStream = new ByteArrayInputStream(cryptoMessage.getMessage());
                 CipherInputStream cipherInputStream = new CipherInputStream(inputStream, cipher)) {

                SecretKey secretKey = generateSecretKey(cryptoMessage.getSalt());
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(cryptoMessage.getIv()));

                byte[] buf = new byte[1024];
                int len;
                while ((len = cipherInputStream.read(buf)) >= 0) {
                    byteArrayOutputStream.write(buf, 0, len);
                }
            }

            return byteArrayOutputStream.toByteArray();

        } catch (Exception e) {
            throw new EncryptException(e);
        }
    }
}
