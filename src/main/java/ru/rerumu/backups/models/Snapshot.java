package ru.rerumu.backups.models;

import ru.rerumu.backups.Generated;

import java.util.Objects;

// TODO: add immutability
public class Snapshot {
    private final static String DELIMITER ="@";
    private final String name;
    private final String dataset;
    private final String fullName;

    public Snapshot(String dataset, String name){
        this.dataset = dataset;
        this.name = name;
        this.fullName = dataset+ DELIMITER +name;
    }

    public Snapshot(String fullName){
        this.fullName = fullName;
        int delimiterIndex = fullName.indexOf(DELIMITER);
        this.dataset = fullName.substring(0,delimiterIndex);
        this.name = fullName.substring(delimiterIndex+1);
    }

    public String getDataset() {
        return dataset;
    }

    public String getFullName() {
        return fullName;
    }

    public String getName() {
        return name;
    }

    @Generated
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Snapshot snapshot = (Snapshot) o;
        return name.equals(snapshot.name) && dataset.equals(snapshot.dataset) && fullName.equals(snapshot.fullName);
    }

    @Generated
    @Override
    public int hashCode() {
        return Objects.hash(name, dataset, fullName);
    }

    @Generated
    @Override
    public String toString() {
        return "Snapshot{" +
                "name='" + name + '\'' +
                ", dataset='" + dataset + '\'' +
                ", fullName='" + fullName + '\'' +
                '}';
    }
}
