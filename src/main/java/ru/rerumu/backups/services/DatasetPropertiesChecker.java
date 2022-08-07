package ru.rerumu.backups.services;

import ru.rerumu.backups.exceptions.IncompatibleDatasetException;
import ru.rerumu.backups.models.ZFSDataset;


public class DatasetPropertiesChecker {

    public void check(ZFSDataset zfsDataset) throws IncompatibleDatasetException {
        if ( !zfsDataset.isEncryptionEnabled()){
            throw new IncompatibleDatasetException();
        }
    }
}
