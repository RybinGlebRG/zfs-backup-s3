package ru.rerumu.backups.factories;

import ru.rerumu.backups.zfs_api.ProcessWrapper;

import java.util.List;

public interface ProcessWrapperFactory {

    ProcessWrapper getProcessWrapper(List<String> args);
}
