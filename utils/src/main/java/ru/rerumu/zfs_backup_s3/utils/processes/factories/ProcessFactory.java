package ru.rerumu.zfs_backup_s3.utils.processes.factories;

import java.io.IOException;
import java.util.List;

public interface ProcessFactory {

    Process create(List<String> args) throws IOException;
}
