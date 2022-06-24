package ru.rerumu.backups.models;

import ru.rerumu.backups.Generated;
import ru.rerumu.backups.exceptions.BaseSnapshotNotFoundException;
import ru.rerumu.backups.exceptions.SnapshotNotFoundException;
import ru.rerumu.backups.models.zfs_dataset_properties.EncryptionProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ZFSDataset {

    private final String name;
    private final List<Snapshot> snapshotList;
    private EncryptionProperty encryption;

    @Deprecated
    public ZFSDataset(String name, List<Snapshot> snapshotList){
        this.name = name;
        this.snapshotList = snapshotList;
    }
    public ZFSDataset(String name, List<Snapshot> snapshotList,EncryptionProperty encryptionProperty){
        this.name = name;
        this.snapshotList = snapshotList;
        this.encryption = encryptionProperty;
    }

    public String getName() {
        return name;
    }

    public Snapshot getBaseSnapshot() throws BaseSnapshotNotFoundException {
        if (snapshotList.size()==0){
            throw new BaseSnapshotNotFoundException();
        }
        return snapshotList.get(0);
    }

    public List<Snapshot> getIncrementalSnapshots(String upperSnapshotName) throws SnapshotNotFoundException {
        List<Snapshot> res = new ArrayList<>();
        int n=0;
        boolean isUpperFound = false;
        for (Snapshot snapshot: snapshotList){
            if (n==0){
                n++;
                continue;
            }
            res.add(snapshot);
            if (snapshot.getName().equals(upperSnapshotName)){
                isUpperFound = true;
                break;
            }
        }
        if (!isUpperFound){
            throw new SnapshotNotFoundException();
        }
        return res;
    }

    public List<Snapshot> getIncrementalSnapshots(String lowerSnapshotName,String upperSnapshotName) throws SnapshotNotFoundException {
        List<Snapshot> res = new ArrayList<>();
        int n=0;
        boolean isLowerFound = false;
        boolean isUpperFound = false;
        for (Snapshot snapshot: snapshotList){
            if (n==0){
                n++;
                continue;
            }
            if (snapshot.getName().equals(lowerSnapshotName)){
                isLowerFound = true;
            }
            if (!isLowerFound){
                continue;
            }
            res.add(snapshot);
            if (snapshot.getName().equals(upperSnapshotName)){
                isUpperFound = true;
                break;
            }
        }
        if (!isLowerFound || !isUpperFound){
            throw new SnapshotNotFoundException();
        }
        return res;
    }

    public boolean isSnapshotExists(String snapshotName){
        for (Snapshot snapshot: snapshotList){
            if (snapshot.getName().equals(snapshotName)){
                return true;
            }
        }
        return false;
    }

    public boolean isEncryptionEnabled() {
        return encryption.equals(EncryptionProperty.ON);
    }

    public void setEncryption(EncryptionProperty encryption) {
        this.encryption = encryption;
    }

    @Generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZFSDataset that = (ZFSDataset) o;
        // TODO: Should check encryption
        return name.equals(that.name) && snapshotList.equals(that.snapshotList);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(name, snapshotList);
    }
}
