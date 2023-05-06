package ru.rerumu.s3.exceptions;

public class IncorrectHashException extends Exception{

    public IncorrectHashException() {
    }

    public IncorrectHashException(String message) {
        super(message);
    }
}
