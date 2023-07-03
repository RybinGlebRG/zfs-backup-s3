package ru.rerumu.zfs_backup_s3.backups.exceptions;

public class ReceiveError extends Error{

    public ReceiveError() {
    }

    public ReceiveError(Throwable cause) {
        super(cause);
    }
}
