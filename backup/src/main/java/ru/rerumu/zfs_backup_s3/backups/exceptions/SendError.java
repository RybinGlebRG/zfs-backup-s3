package ru.rerumu.zfs_backup_s3.backups.exceptions;

public class SendError extends Error{
    public SendError() {
    }

    public SendError(Throwable cause) {
        super(cause);
    }
}
