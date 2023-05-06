package ru.rerumu.zfs.models;


public record Snapshot(String dataset, String name, String fullName) {
    private final static String DELIMITER ="@";

    public Snapshot(String dataset, String name){
        this(dataset,name,dataset+ DELIMITER +name);
    }

    public Snapshot(String fullName){
        this(
                fullName.substring(0,fullName.indexOf(DELIMITER)),
                fullName.substring(fullName.indexOf(DELIMITER)+1),
                fullName
        );
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
}
