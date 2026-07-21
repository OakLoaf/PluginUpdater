package org.lushplugins.pluginupdater.api.exception;

public class FailedAttemptsOnSourcesException extends Exception {

    public FailedAttemptsOnSourcesException(String message) {
        super(message, null, false, false);
    }
}
