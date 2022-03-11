package ru.rerumu.backups.services.impl;

import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.services.Compressor;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPCompressor implements Compressor {

    @Override
    public byte[] compressChunk(byte[] chunk) throws CompressorException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                gzipOutputStream.write(chunk);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new CompressorException(e);
        }

    }

    @Override
    public byte[] decompressChunk(byte[] chunk) throws CompressorException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (InputStream inputStream = new ByteArrayInputStream(chunk);
                 GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream)) {

                byte[] buf = new byte[1024];
                int len;
                while ((len = gzipInputStream.read(buf)) >= 0) {
                    byteArrayOutputStream.write(buf, 0, len);
                }
            }
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new CompressorException(e);
        }
    }
}
