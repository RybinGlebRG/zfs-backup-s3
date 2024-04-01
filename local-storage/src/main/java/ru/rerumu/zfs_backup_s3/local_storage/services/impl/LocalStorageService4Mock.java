package ru.rerumu.zfs_backup_s3.local_storage.services.impl;

import ru.rerumu.zfs_backup_s3.local_storage.services.LocalStorageService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

public final class LocalStorageService4Mock implements LocalStorageService {
    @Override
    public void send(BufferedInputStream bufferedInputStream, String prefix) {

    }

    @Override
    public void sendExisting(String prefix) {

    }

    @Override
    public boolean areFilesPresent() {
        return false;
    }

    @Override
    public void receive(String prefix, BufferedOutputStream bufferedOutputStream) {

    }
}
