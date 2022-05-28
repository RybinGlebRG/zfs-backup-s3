package ru.rerumu.backups.services;

import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.io.S3Loader;
import ru.rerumu.backups.models.Snapshot;

import java.io.IOException;
import java.util.List;

public interface SnapshotSender {

    void sendBaseSnapshot(Snapshot baseSnapshot, S3Loader s3Loader, boolean isLoadS3)
            throws InterruptedException, CompressorException, IOException, EncryptException;

    void sendIncrementalSnapshot(Snapshot baseSnapshot, Snapshot incrementalSnapshot, S3Loader s3Loader, boolean isLoadS3)
            throws InterruptedException, CompressorException, IOException, EncryptException;

    void checkSent(List<Snapshot> snapshotList, S3Loader s3Loader);
}
