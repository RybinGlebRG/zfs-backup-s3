package ru.rerumu.backups.services;

import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.exceptions.FileHitSizeLimitException;
import ru.rerumu.backups.exceptions.ZFSStreamEndedException;
import ru.rerumu.backups.zfs_api.ZFSSend;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Path;

public interface ZFSFileWriter {

    void write(BufferedInputStream bufferedInputStream) throws IOException,
            CompressorException,
            EncryptException,
            FileHitSizeLimitException,
            ZFSStreamEndedException;
}
