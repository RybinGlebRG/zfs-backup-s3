package ru.rerumu.backups.services;

import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.exceptions.IncorrectHashException;
import ru.rerumu.backups.exceptions.S3MissesFileException;
import ru.rerumu.backups.models.Snapshot;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Deprecated
public interface SnapshotSender {

//    @Deprecated
//    void sendBaseSnapshot(Snapshot baseSnapshot, S3Loader s3Loader, boolean isLoadS3)
//            throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException;
//
//    @Deprecated
//    void sendIncrementalSnapshot(Snapshot baseSnapshot, Snapshot incrementalSnapshot, S3Loader s3Loader, boolean isLoadS3)
//            throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException;

    void sendStartingFromFull(String datasetName, List<Snapshot> snapshotList)
            throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException, S3MissesFileException;
    void sendStartingFromIncremental(String datasetName, List<Snapshot> snapshotList)
            throws InterruptedException, CompressorException, IOException, EncryptException, NoSuchAlgorithmException, IncorrectHashException, ExecutionException, S3MissesFileException;
}
