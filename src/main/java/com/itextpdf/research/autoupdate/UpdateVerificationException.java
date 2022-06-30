package com.itextpdf.research.autoupdate;

public class UpdateVerificationException extends Exception {
    public UpdateVerificationException(String message) {
        super(message);
    }

    public UpdateVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
