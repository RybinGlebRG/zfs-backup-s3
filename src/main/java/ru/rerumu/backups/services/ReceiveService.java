package ru.rerumu.backups.services;

import ru.rerumu.backups.models.ZFSPool;

import java.io.IOException;

public interface ReceiveService {

    void receive(String prefix, ZFSPool zfsPool);
}
