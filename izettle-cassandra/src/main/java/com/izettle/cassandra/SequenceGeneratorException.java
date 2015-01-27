package com.izettle.cassandra;

public class SequenceGeneratorException extends Exception {

    public SequenceGeneratorException(String message, Throwable cause) {
        super(message, cause);
    }

    public SequenceGeneratorException(String message) {
        super(message);
    }
}
