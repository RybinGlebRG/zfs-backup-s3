package ru.rerumu.backups.services.zfs.factories;

import ru.rerumu.backups.services.zfs.ZFSService;

public interface ZFSServiceFactory {

    ZFSService getZFSService();
}
