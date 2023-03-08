package ru.rerumu.backups.services;

import ru.rerumu.backups.exceptions.CompressorException;
import ru.rerumu.backups.exceptions.EncryptException;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;

import java.io.IOException;

public interface SendService {

    void send(Snapshot snapshot) throws IOException, CompressorException, EncryptException;
}
