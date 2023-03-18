package ru.rerumu.backups.models.zfs;

import ru.rerumu.backups.models.ZFSDataset;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public record Pool(String name, List<ZFSDataset> datasetList) {

    public Optional<ZFSDataset> getRootDataset(){
        return datasetList.stream().findFirst();
    }
}
