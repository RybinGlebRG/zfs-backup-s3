package ru.rerumu.backups.zfs_api.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.errors.ProcessRunError;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.zfs.SnapshotCommand;

import java.util.ArrayList;
import java.util.List;

public class SnapshotCommandImpl implements SnapshotCommand {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProcessWrapper processWrapper;
    private final List<String> command;

    private SnapshotCommandImpl(
            Boolean isRecursive,
            ZFSDataset dataset,
            String name,
            ProcessWrapperFactory processWrapperFactory
    ){
        if (isRecursive == null || dataset == null || name == null || processWrapperFactory == null){
            throw new IllegalArgumentException();
        }

        command = new ArrayList<>();
        command.add("zfs");
        command.add("snapshot");
        if (isRecursive){
            command.add("-r");
        }
        command.add(dataset.name()+"@"+name);

        processWrapper = processWrapperFactory.getProcessWrapper(command);
    }

    @Override
    public void execute() {
        try {
            processWrapper.run();
            processWrapper.setStderrProcessor(logger::error);
            processWrapper.close();
        } catch (Exception e){
            throw new ProcessRunError(e);
        }
    }

    public static class Builder{

        private Boolean isRecursive;
        private ZFSDataset dataset;
        private String name;
        private ProcessWrapperFactory processWrapperFactory;

        public Builder recursive(){
            isRecursive = true;
            return this;
        }

        public Builder dataset(ZFSDataset dataset){
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



        public SnapshotCommandImpl build(){
            return new SnapshotCommandImpl(isRecursive,dataset,name,processWrapperFactory);
        }
    }
}
