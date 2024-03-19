package ru.rerumu.zfs_backup_s3.cli;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.zfs_backup_s3.backups.EntityFactory;
import ru.rerumu.zfs_backup_s3.backups.services.ReceiveService;
import ru.rerumu.zfs_backup_s3.backups.services.SendService;

import java.lang.module.Configuration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CliService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EntityFactory entityFactory;

    public CliService(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    /**
     * TODO: Add "--skipFilesGeneration" option. Meant to be used with independent upload when files already has been generated
     */
    public void run(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("h", "help", false, "Print this message");
        options.addOption("c", "continue", true, "Continue backup");

        Option backupFullOption = Option.builder().longOpt("backupFull")
                .desc("Create full backup")
                .build();
        options.addOption(backupFullOption);

        Option restoreOption = Option.builder().longOpt("restore")
                .desc("Restore full backup")
                .build();
        options.addOption(restoreOption);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        HelpFormatter formatter = new HelpFormatter();
        List<String> positional = cmd.getArgList();

        logger.info(String.format("Args: %s", Arrays.toString(cmd.getArgs())));
        logger.info(String.format("Positional: %s", positional));

        if (cmd.hasOption("h")) {
            formatter.printHelp("zfs-s3-backup", options);
            return;
        }

        String poolName;
        String bucketName;
        Mode mode;
        String continueSnapshotName = null;


        List<Boolean> modes = new ArrayList<>();
        modes.add(cmd.hasOption(backupFullOption));
        modes.add(cmd.hasOption(restoreOption));

        long modesSpecified = modes.stream()
                .filter(Boolean::booleanValue)
                .count();
        if (modesSpecified == 0) {
            throw new IllegalArgumentException("Al least one mode must be specified");
        } else if (modesSpecified > 1) {
            throw new IllegalArgumentException("Multiple modes cannot be specified");
        } else if (positional.size() != 2) {
            throw new IllegalArgumentException("Pool and/or s3 bucket are not specified");
        } else if (cmd.hasOption(backupFullOption)) {
            mode = Mode.BACKUP_FULL;
            poolName = positional.get(0);
            bucketName = positional.get(1);
        } else if (cmd.hasOption(restoreOption)) {
            mode = Mode.RESTORE;
            bucketName = positional.get(0);
            poolName = positional.get(1);
        } else {
            throw new AssertionError("Something went horribly wrong");
        }

        if (cmd.hasOption("c")) {
            continueSnapshotName = cmd.getOptionValue("c");
        }

        logger.info("Starting");
        logger.info("Mode is '" + mode + "'");
        logger.info(String.format("continueSnapshotName='%s'", continueSnapshotName));


        switch (mode) {
            case BACKUP_FULL -> {
                SendService sendService = entityFactory.getSendService(bucketName);
                sendService.send(poolName, bucketName, continueSnapshotName);
            }
            case RESTORE -> {
                ReceiveService receiveService = entityFactory.getReceiveService(bucketName);
                receiveService.receive(bucketName, poolName);
            }
        }

        logger.info("Finished");
    }
}