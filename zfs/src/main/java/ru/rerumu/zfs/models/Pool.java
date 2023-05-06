package ru.rerumu.zfs.models;

import java.util.ArrayList;
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
