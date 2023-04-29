package ru.rerumu.backups.models.zfs;

import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.services.zfs.ZFSService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public record Pool(String name, List<Dataset> datasetList) {

    public Pool{
        datasetList = new ArrayList<>(datasetList);
    }

    public Optional<Dataset> getRootDataset(){
        return datasetList.stream().findFirst();
    }
}
