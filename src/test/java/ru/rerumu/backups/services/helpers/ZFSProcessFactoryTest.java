package ru.rerumu.backups.services.helpers;

import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSPool;
import ru.rerumu.backups.services.ZFSProcessFactory;
import ru.rerumu.backups.zfs_api.*;
import ru.rerumu.backups.zfs_api.helpers.ZFSListFilesystemsTest;
import ru.rerumu.backups.zfs_api.helpers.ZFSListSnapshotsTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ZFSProcessFactoryTest implements ZFSProcessFactory {

    private List<String> stringList;
    private List<String> filesystems;
    private List<ZFSStreamTest> zfsStreamTests;
    private HashMap<String, byte[]> snapshotsWithStream;
    private ZFSReceiveTest zfsReceiveTest;

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public void setFilesystems(List<String> filesystems) {
        this.filesystems = filesystems;
    }

    public void setZfsStreamTests(List<ZFSStreamTest> zfsStreamTests) {
        this.zfsStreamTests = zfsStreamTests;
    }

    public void setSnapshots(List<String> snapshots, List<ZFSStreamTest> streams){
        if (snapshots.size()!=streams.size()){
            throw new IllegalArgumentException();
        }
        int n=0;
        this.snapshotsWithStream = new HashMap<>();
        for (String snapshot: snapshots){
            snapshotsWithStream.put(snapshot,streams.get(n).getData());
            n++;
        }
    }

    public HashMap<String, byte[]> getSnapshotsWithStream() {
        return snapshotsWithStream;
    }

    @Override
    public ProcessWrapper getZFSListSnapshots(String fileSystemName) throws IOException {
        return new ZFSListSnapshotsTest(stringList);
    }

    @Override
    public ProcessWrapper getZFSListFilesystems(String parentFileSystem) throws IOException {
        return new ZFSListFilesystemsTest(filesystems);
    }

    @Override
    public ZFSReceive getZFSReceive(ZFSPool zfsPool) throws IOException {
        ZFSReceiveTest zfsReceiveTest = new ZFSReceiveTest();
        this.zfsReceiveTest = zfsReceiveTest;
        return zfsReceiveTest;
    }

    public ZFSReceiveTest getZFSReceive(){
        return zfsReceiveTest;
    }

    @Override
    public ZFSSend getZFSSendIncremental(Snapshot baseSnapshot, Snapshot incrementalSnapshot) throws IOException {
        ZFSSendTest zfsSendTest = new ZFSSendTest(snapshotsWithStream.get(incrementalSnapshot.getFullName()));
        return zfsSendTest;
    }

    @Override
    public ZFSSend getZFSSendFull(Snapshot snapshot) throws IOException {
        ZFSSendTest zfsSendTest = new ZFSSendTest(snapshotsWithStream.get(snapshot.getFullName()));
        return zfsSendTest;
    }
}
