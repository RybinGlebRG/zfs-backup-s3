package ru.rerumu.backups.services;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.services.impl.ZFSSendFull;
import ru.rerumu.backups.services.impl.ZFSSendFullRecursive;

import java.io.IOException;

public class ZFSProcessFactory {

    public ZFSSend getZFSSendFull(Snapshot snapshot) throws IOException {
        return new ZFSSendFull(snapshot);
    }

    public ZFSSend getZFSSendIncremental(Snapshot baseSnapshot, Snapshot incrementalSnapshot){

    }


}
