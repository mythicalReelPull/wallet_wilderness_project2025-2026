package org.example.exception;

public class InvalidTransactionException extends Exception {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
