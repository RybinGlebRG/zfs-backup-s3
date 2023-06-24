package ru.rerumu.main;

import ru.rerumu.zfs_backup_s3.backups.EntityFactory;
import ru.rerumu.zfs_backup_s3.backups.services.ReceiveService;
import ru.rerumu.zfs_backup_s3.backups.services.SendService;
import ru.rerumu.zfs_backup_s3.cli.CliService;
import software.amazon.awssdk.regions.Region;

import java.net.URI;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        try {
            Configuration configuration = new Configuration();
            EntityFactory entityFactory = new EntityFactory();
            SendService sendService = entityFactory.getSendService(
                    configuration.region(),

                    // TODO: Should be read from command line
                    configuration.bucketName(),
                    configuration.keyId(),
                    configuration.secretKey(),
                    configuration.prefix(),
                    configuration.endpoint(),
                    configuration.storageClass(),
                    configuration.maxPartSize(),
                    configuration.filePartSize(),
                    configuration.tempDir()
            );
            ReceiveService receiveService = entityFactory.getReceiveService(
                    configuration.region(),
                    configuration.bucketName(),
                    configuration.keyId(),
                    configuration.secretKey(),
                    configuration.prefix(),
                    configuration.endpoint(),
                    configuration.storageClass(),
                    configuration.maxPartSize(),
                    configuration.filePartSize(),
                    configuration.tempDir()
            );
            CliService cliService = new CliService(sendService, receiveService);
            cliService.run(args);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}