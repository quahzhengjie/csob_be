package com.bkb.scanner.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to verify a document they uploaded themselves.
 * This helps maintain the integrity of the document verification process.
 */
@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Users cannot verify documents they uploaded themselves")
public class SelfVerificationException extends RuntimeException {

    public SelfVerificationException() {
        super("Users cannot verify documents they uploaded themselves");
    }

    public SelfVerificationException(String documentId, String username) {
        super(String.format("User '%s' cannot verify document ID %s because they uploaded it", username, documentId));
    }
}