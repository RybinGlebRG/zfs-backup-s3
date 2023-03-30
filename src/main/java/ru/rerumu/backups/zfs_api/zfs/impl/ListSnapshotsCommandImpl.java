package ru.rerumu.backups.zfs_api.zfs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.exceptions.errors.ProcessRunError;
import ru.rerumu.backups.factories.ProcessWrapperFactory;
import ru.rerumu.backups.models.Snapshot;
import ru.rerumu.backups.models.ZFSDataset;
import ru.rerumu.backups.zfs_api.ProcessWrapper;
import ru.rerumu.backups.zfs_api.zfs.ListSnapshotsCommand;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListSnapshotsCommandImpl implements ListSnapshotsCommand {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ProcessWrapper processWrapper;
    private final List<String> command;
    private final ZFSDataset dataset;

    public ListSnapshotsCommandImpl(ZFSDataset dataset, ProcessWrapperFactory processWrapperFactory) {
        if (dataset == null || processWrapperFactory == null){
            throw new IllegalArgumentException();
        }

        this.dataset = dataset;

        command = new ArrayList<>();
        command.add("zfs");
        command.add("list");
        command.add("-rH");
        command.add("-t");
        command.add("snapshot");
        command.add("-o");
        command.add("name");
        command.add("-s");
        command.add("creation");
        command.add("-d");
        command.add("1");


        processWrapper = processWrapperFactory.getProcessWrapper(command);
    }

    @Override
    public List<Snapshot> execute() {
        byte[] output;
        try {
            processWrapper.run();
            processWrapper.setStderrProcessor(logger::error);
            output = processWrapper.getBufferedInputStream().readAllBytes();
            processWrapper.close();
        } catch (Exception e){
            throw new ProcessRunError(e);
        }

        String str = new String(output, StandardCharsets.UTF_8);
        logger.debug(String.format("Got from process: \n%s",str));
        String[] lines = str.split("\\n");

        List<Snapshot> snapshotList = new ArrayList<>();

        return Arrays.stream(lines)
                .map(String::strip)
                .map(Snapshot::new)
                .filter(item->item.getDataset().equals(dataset.getName()))
                .peek(item -> logger.debug(String.format("Got snapshot: %s",item.getFullName())))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
