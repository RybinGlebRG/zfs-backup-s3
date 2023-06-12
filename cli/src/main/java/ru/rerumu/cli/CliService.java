package ru.rerumu.cli;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rerumu.backups.services.ReceiveService;
import ru.rerumu.backups.services.SendService;

public class CliService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final SendService sendService;
    private final ReceiveService receiveService;

    public CliService(SendService sendService, ReceiveService receiveService) {
        this.sendService = sendService;
        this.receiveService = receiveService;
    }

    public void run(String[] args) throws Exception {
        Options options = new Options();
        options.addOption("p", "pool", true, "pool to backup");
        options.addOption("b", "bucket", true, "S3 Bucket in which to store backup");
        options.addOption("h", "help", true, "print this message");
        options.addOption("m", "mode", true, "'full' for full backup");
        options.addOption("s", "snapshot", true, "snapshot to restore");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        HelpFormatter formatter = new HelpFormatter();

        if (cmd.hasOption("h")) {
            formatter.printHelp("zfs-s3-backup", options);
            return;
        }

        String poolName;
        String bucketName;
        String mode = "backupFull";
        String snapshotName = null;

        if (cmd.hasOption("p") && cmd.hasOption("b")) {
            poolName = cmd.getOptionValue("p");
            bucketName = cmd.getOptionValue("b");
        } else {
            throw new IllegalArgumentException("Pool and/or s3 bucket are not specified");
        }

        if(cmd.hasOption("m") ){
            mode = switch (cmd.getOptionValue("m")){
                case "full" ->"backupFull";
                case "restore" -> "restore";
                default -> throw new IllegalArgumentException("Incorrect mode value");
            };
        }

        if (mode.equals("restore")){
            if (cmd.hasOption("s")) {
                snapshotName = cmd.getOptionValue("s");
            } else {
                throw new IllegalArgumentException("Snapshot is not specified");
            }
        }

        logger.info("Starting");
        logger.info("Mode is '" + mode + "'");

        switch (mode) {
            case "backupFull" -> sendService.send(poolName, bucketName);
            case "restore" -> receiveService.receive(bucketName, poolName);
            default -> throw new IllegalArgumentException();
        }

        logger.info("Finished");
    }
}