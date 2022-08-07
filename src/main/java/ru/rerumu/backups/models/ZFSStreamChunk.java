package ru.rerumu.backups.models;

import java.io.Serializable;

public class ZFSStreamChunk implements Serializable {
    private static final long serialVersionUID = 1L;
    private final byte[] chunk;

    public ZFSStreamChunk(byte[] chunk){
        this.chunk = chunk;
    }

    public byte[] getChunk() {
        return chunk;
    }

}
