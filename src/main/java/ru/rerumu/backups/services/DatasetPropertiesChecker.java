package ru.rerumu.backups.services;

import ru.rerumu.backups.exceptions.IncompatibleDatasetException;
import ru.rerumu.backups.models.ZFSDataset;

public class DatasetPropertiesChecker {
    private final boolean isNativeEncrypted;

    public DatasetPropertiesChecker(boolean isNativeEncrypted){
        this.isNativeEncrypted = isNativeEncrypted;
    }

    public void check(ZFSDataset zfsDataset) throws IncompatibleDatasetException {
        if (isNativeEncrypted && !zfsDataset.isEncrypted()){
            throw new IncompatibleDatasetException();
        }
    }
}
