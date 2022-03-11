package ru.rerumu.backups.models;

import java.io.Serializable;

public class CryptoMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private final byte[] salt;
    private final byte[] iv;
    private final byte[] message;

    public CryptoMessage(byte[] salt, byte[] iv, byte[] message){
        this.salt = salt;
        this.iv = iv;
        this.message = message;
    }

    public byte[] getMessage() {
        return message;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getIv() {
        return iv;
    }
}
