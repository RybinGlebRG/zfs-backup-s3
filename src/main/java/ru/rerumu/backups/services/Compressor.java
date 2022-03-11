package ru.rerumu.backups.services;

import ru.rerumu.backups.exceptions.CompressorException;

import java.io.File;
import java.nio.file.Path;

public interface Compressor {

    byte[] compressChunk(byte[] chunk) throws CompressorException;
    byte[] decompressChunk(byte[] chunk) throws CompressorException;
}
