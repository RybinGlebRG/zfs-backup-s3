package ru.rerumu.zfs_backup_s3.local_storage.services;

import ru.rerumu.zfs_backup_s3.local_storage.services.impl.ConsecutiveLocalStorageService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public sealed interface LocalStorageService permits ConsecutiveLocalStorageService, ru.rerumu.zfs_backup_s3.local_storage.services.impl.LocalStorageService4Mock {
    void send(BufferedInputStream bufferedInputStream, String prefix);
    void sendExisting(String prefix);

    boolean areFilesPresent();
    void receive(String prefix, BufferedOutputStream bufferedOutputStream);
}
