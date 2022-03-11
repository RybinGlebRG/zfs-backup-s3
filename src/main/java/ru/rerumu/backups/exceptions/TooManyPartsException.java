package ru.rerumu.backups.exceptions;

public class TooManyPartsException extends Exception {

    public TooManyPartsException(Throwable e) {
        super(e);
    }

    public TooManyPartsException(String message) {
        super(message);
    }

    public TooManyPartsException() {
        super();
    }
}
