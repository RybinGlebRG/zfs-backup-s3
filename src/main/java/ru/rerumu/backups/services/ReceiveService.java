package ru.rerumu.backups.services;

public interface ReceiveService {

    void receive(String bucketName, String poolName);
}
