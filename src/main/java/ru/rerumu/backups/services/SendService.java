package ru.rerumu.backups.services;

import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.exceptions.S3MissesFileException;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.models.s3.Bucket;
import ru.rerumu.backups.models.zfs.Pool;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutionException;

public interface SendService {

    void send(Pool pool, Bucket bucket);
}
