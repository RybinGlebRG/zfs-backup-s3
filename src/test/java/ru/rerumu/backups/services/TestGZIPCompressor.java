package ru.rerumu.backups.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.rerumu.backups.services.Compressor;
import ru.rerumu.backups.services.impl.GZIPCompressor;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public class TestGZIPCompressor {

    @Test
    void encryptDecrypt1500() throws Exception {
        byte[] src = new byte[1500];
        Compressor compressor = new GZIPCompressor();
        Compressor compressor2 = new GZIPCompressor();
        byte[] tmp = compressor.compressChunk(src);
        byte[] dst = compressor2.decompressChunk(tmp);

        Assertions.assertArrayEquals(src, dst);

    }

    @Test
    void encryptDecrypt1000() throws Exception {
        byte[] src = new byte[1000];
        Compressor compressor = new GZIPCompressor();
        Compressor compressor2 = new GZIPCompressor();
        byte[] tmp = compressor.compressChunk(src);
        byte[] dst = compressor2.decompressChunk(tmp);

        Assertions.assertArrayEquals(src, dst);

    }

    @Test
    void encryptDecrypt1024() throws Exception {
        byte[] src = new byte[1024];
        Compressor compressor = new GZIPCompressor();
        Compressor compressor2 = new GZIPCompressor();
        byte[] tmp = compressor.compressChunk(src);
        byte[] dst = compressor2.decompressChunk(tmp);

        Assertions.assertArrayEquals(src, dst);

    }

}
