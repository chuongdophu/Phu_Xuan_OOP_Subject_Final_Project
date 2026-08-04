package com.autoparking.exception;

public class InvalidTicketException extends Exception {
    public InvalidTicketException(String message) {
        super(message);
    }
}