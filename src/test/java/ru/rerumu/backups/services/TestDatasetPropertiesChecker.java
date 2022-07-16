package ru.rerumu.backups.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.rerumu.backups.exceptions.IncompatibleDatasetException;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.models.zfs_dataset_properties.EncryptionProperty;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class TestDatasetPropertiesChecker {

    @Test
    void shouldPassCheck() throws Exception{
        DatasetPropertiesChecker datasetPropertiesChecker = new DatasetPropertiesChecker();
        datasetPropertiesChecker.check(
                new ZFSDataset("test",new ArrayList<>(), EncryptionProperty.ON)
        );
    }

    @Test
    void shouldNotCheck() throws Exception{
        DatasetPropertiesChecker datasetPropertiesChecker = new DatasetPropertiesChecker();
        Assertions.assertThrows(
                IncompatibleDatasetException.class,
                ()-> datasetPropertiesChecker.check(
                        new ZFSDataset("test",new ArrayList<>(), EncryptionProperty.OFF)
                ));

    }
}