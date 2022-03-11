package ru.rerumu.backups.services;

import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.models.CryptoMessage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface Cryptor {

    CryptoMessage encryptChunk(byte[] chunk) throws EncryptException;
    byte[] decryptChunk(CryptoMessage cryptoMessage) throws EncryptException;
}
