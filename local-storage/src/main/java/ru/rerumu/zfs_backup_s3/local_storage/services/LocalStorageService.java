package ru.rerumu.zfs_backup_s3.local_storage.services;

import ru.rerumu.zfs_backup_s3.local_storage.services.impl.ConsecutiveLocalStorageService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public sealed interface LocalStorageService permits ConsecutiveLocalStorageService {
    void send(BufferedInputStream bufferedInputStream, Consumer<Path> fileConsumer);
    void receive(List<String> keys, BiConsumer<String, Path> fileDownloader, BufferedOutputStream bufferedOutputStream);
}
