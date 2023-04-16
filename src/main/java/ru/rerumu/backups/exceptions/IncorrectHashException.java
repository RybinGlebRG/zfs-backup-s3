package ru.rerumu.backups.exceptions;

public class IncorrectHashException extends Exception{

    public IncorrectHashException() {
    }

    public IncorrectHashException(String message) {
        super(message);
    }
}
