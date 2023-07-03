package ru.rerumu.zfs_backup_s3.s3.exceptions;

public class IncorrectHashException extends Exception{

    public IncorrectHashException() {
    }

    public IncorrectHashException(String message) {
        super(message);
    }
}
