package ru.rerumu.backups.models;

import ru.rerumu.backups.exceptions.BaseSnapshotNotFoundException;
import ru.rerumu.backups.exceptions.SnapshotNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class ZFSFileSystem {

    private final String name;
    private final List<Snapshot> snapshotList;

    public ZFSFileSystem(String name, List<Snapshot> snapshotList){
        this.name = name;
        this.snapshotList = snapshotList;
    }

    public List<Snapshot> getSnapshotList() {
        return snapshotList;
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

    public List<Snapshot> getIncrementalSnapshots(){
        List<Snapshot> res = new ArrayList<>();
        int n=0;
        for (Snapshot snapshot: snapshotList){
            if (n==0){
                n++;
                continue;
            }
            res.add(snapshot);
        }
        return res;
    }

    public List<Snapshot> getIncrementalSnapshots(String upperSnapshotName){
        List<Snapshot> res = new ArrayList<>();
        int n=0;
        for (Snapshot snapshot: snapshotList){
            if (n==0){
                n++;
                continue;
            }
            res.add(snapshot);
            if (snapshot.getName().equals(upperSnapshotName)){
                break;
            }
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
}
