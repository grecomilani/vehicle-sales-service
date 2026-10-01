package com.grecomilani.vehiclesales.domain.exception;

/**
 * Recurso solicitado não existe.
 */
public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(message);
    }
}
