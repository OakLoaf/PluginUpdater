package org.lushplugins.pluginupdater.api.exception;

public class InvalidVersionFormatException extends RuntimeException {

    public InvalidVersionFormatException(String message) {
        super(message);
    }

    public InvalidVersionFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
