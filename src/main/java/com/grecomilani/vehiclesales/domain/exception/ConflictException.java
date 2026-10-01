package com.grecomilani.vehiclesales.domain.exception;

/**
 * Operação incompatível com o estado atual do recurso.
 */
public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(message);
    }
}
