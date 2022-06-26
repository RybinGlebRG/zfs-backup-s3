package ru.rerumu.backups.zfs_api.impl;

import ru.rerumu.backups.zfs_api.ZFSGetDatasetProperty;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ZFSGetDatasetPropertyImpl extends ProcessWrapperImpl implements ZFSGetDatasetProperty {

    public ZFSGetDatasetPropertyImpl(String propertyName, String datasetName) throws IOException {
        super(Arrays.asList(
                "zfs","get","-Hp","-d","0","-t","filesystem,volume","-o","value",propertyName,datasetName
        ));
    }

}
