package org.lushplugins.pluginupdater.api.exception;

public class FailedAttemptsOnSourcesException extends Exception {

    public FailedAttemptsOnSourcesException(String message) {
        super(message, null, false, false);
    }

    public FailedAttemptsOnSourcesException(String message, Throwable cause) {
        super(message, cause, false, false); // TODO: Check whether stack trace needs to be writable here
    }
}
