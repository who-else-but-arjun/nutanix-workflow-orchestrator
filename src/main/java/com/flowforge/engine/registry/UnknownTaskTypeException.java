package com.flowforge.engine.registry;

public class UnknownTaskTypeException extends RuntimeException {
    public UnknownTaskTypeException(String type) {
        super("Unknown task type: " + type);
    }
}
