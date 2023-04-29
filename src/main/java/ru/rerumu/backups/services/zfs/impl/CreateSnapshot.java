package ru.rerumu.backups.services.zfs.impl;

import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.zfs.Dataset;
import ru.rerumu.backups.zfs_api.ProcessWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class CreateSnapshot implements Callable<Void> {
    private final Dataset dataset;
    private final String name;
    private final Boolean isRecursive;
    private final ProcessWrapperFactory processWrapperFactory;


    public CreateSnapshot(Dataset dataset, String name, Boolean isRecursive, ProcessWrapperFactory processWrapperFactory) {
        if (isRecursive == null || dataset == null || name == null || processWrapperFactory == null){
            throw new IllegalArgumentException();
        }
        this.dataset = dataset;
        this.name = name;
        this.isRecursive = isRecursive;
        this.processWrapperFactory = processWrapperFactory;
    }

    @Override
    public Void call() throws Exception {
        List<String> command = new ArrayList<>();
        command.add("zfs");
        command.add("snapshot");
        if (isRecursive){
            command.add("-r");
        }
        command.add(dataset.name()+"@"+name);

        ProcessWrapper processWrapper = processWrapperFactory.getProcessWrapper(command);
        return null;
    }

    public static class Builder{

        private Boolean isRecursive=false;
        private Dataset dataset;
        private String name;
        private ProcessWrapperFactory processWrapperFactory;

        public Builder recursive(){
            isRecursive = true;
            return this;
        }

        public Builder dataset(Dataset dataset){
            this.dataset = dataset;
            return this;
        }

        public Builder name(String name){
            this.name = name;
            return this;
        }

        public Builder processWrapperFactory(ProcessWrapperFactory processWrapperFactory){
            this.processWrapperFactory = processWrapperFactory;
            return this;
        }



        public CreateSnapshot build(){
            return new CreateSnapshot(dataset,name,isRecursive,processWrapperFactory);
        }
    }
}
