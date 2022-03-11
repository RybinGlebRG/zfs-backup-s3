package ru.rerumu.backups.exceptions;

public class NoMorePartsException extends Exception {

    public NoMorePartsException(Throwable e) {
        super(e);
    }

    public NoMorePartsException(String message) {
        super(message);
    }

    public NoMorePartsException() {
        super();
    }
}
