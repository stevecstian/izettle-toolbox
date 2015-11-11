package com.izettle.jdbi.exception;

public class NoResultException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NoResultException(String message) {
        super(message);
    }
}
