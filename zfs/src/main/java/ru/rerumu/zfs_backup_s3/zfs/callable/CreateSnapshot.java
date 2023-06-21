package ru.rerumu.zfs_backup_s3.zfs.callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.utils.CallableOnlyOnce;
import ru.rerumu.zfs_backup_s3.utils.ThreadSafe;
import ru.rerumu.zfs_backup_s3.utils.processes.StdLineConsumer;
import ru.rerumu.zfs_backup_s3.utils.processes.factories.ProcessWrapperFactory;
import ru.rerumu.zfs_backup_s3.utils.processes.impl.StdProcessorImpl;
import ru.rerumu.zfs_backup_s3.zfs.models.Dataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;

@ThreadSafe
public final class CreateSnapshot extends CallableOnlyOnce<Void> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Dataset dataset;
    private final String name;
    private final Boolean isRecursive;
    private final ProcessWrapperFactory processWrapperFactory;


    public CreateSnapshot(Dataset dataset, String name, Boolean isRecursive, ProcessWrapperFactory processWrapperFactory) {
        Objects.requireNonNull(dataset,"dataset cannot be null");
        Objects.requireNonNull(name,"name cannot be null");
        Objects.requireNonNull(isRecursive,"isRecursive cannot be null");
        Objects.requireNonNull(processWrapperFactory,"processWrapperFactory cannot be null");
        this.dataset = dataset;
        this.name = name;
        this.isRecursive = isRecursive;
        this.processWrapperFactory = processWrapperFactory;
    }

    @Override
    protected Void callOnce() throws Exception {
        List<String> command = new ArrayList<>();
        command.add("zfs");
        command.add("snapshot");
        if (isRecursive) {
            command.add("-r");
        }
        command.add(dataset.name() + "@" + name);

        processWrapperFactory.getProcessWrapper(
                command,
                new StdProcessorImpl(
                        new StdLineConsumer(logger::error),
                        new StdLineConsumer(logger::debug),
                        null
                )
        ).call();

        return null;
    }
}
