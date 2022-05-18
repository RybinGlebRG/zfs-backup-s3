package ru.rerumu.backups.models;

public class ZFSPool {
    private final String name;

    public ZFSPool(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
