package com.izettle.cassandra;

public class SequenceGeneratorException extends Exception {

    private static final long serialVersionUID = -5230816030539860863L;

    public SequenceGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public SequenceGeneratorException(String message) {
        super(message);
    }
}
