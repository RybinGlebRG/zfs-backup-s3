package ru.rerumu.backups.models;

public class ZFSFileSystem {

    private final String name;

    public ZFSFileSystem(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
