package ru.rerumu.main;

import ru.rerumu.zfs_backup_s3.backups.EntityFactory;
import ru.rerumu.zfs_backup_s3.backups.services.ReceiveService;
import ru.rerumu.zfs_backup_s3.backups.services.SendService;
import ru.rerumu.zfs_backup_s3.cli.CliService;

public class Main {
    public static void main(String[] args) {
        try {
            EntityFactory entityFactory = new EntityFactory();
            SendService sendService = entityFactory.getSendService();
            ReceiveService receiveService = entityFactory.getReceiveService();
            CliService cliService = new CliService(sendService, receiveService);
            cliService.run(args);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}