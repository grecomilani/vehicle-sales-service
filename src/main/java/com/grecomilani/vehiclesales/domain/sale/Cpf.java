package com.grecomilani.vehiclesales.domain.sale;

import com.grecomilani.vehiclesales.domain.exception.DomainException;

/**
 * CPF do comprador, armazenado apenas com os 11 dígitos.
 * Aceita a entrada com ou sem máscara ({@code 529.982.247-25} ou {@code 52998224725}).
 */
public record Cpf(String value) {

    public Cpf {
        String digits = value == null ? "" : value.replaceAll("[.\\-\\s]", "");
        if (!isValid(digits)) {
            throw new DomainException("CPF inválido.");
        }
        value = digits;
    }

    private static boolean isValid(String digits) {
        if (!digits.matches("\\d{11}") || digits.chars().distinct().count() == 1) {
            return false;
        }
        return checkDigit(digits, 9) == digits.charAt(9) - '0'
                && checkDigit(digits, 10) == digits.charAt(10) - '0';
    }

    /**
     * Calcula o dígito verificador a partir dos {@code length} primeiros dígitos.
     */
    private static int checkDigit(String digits, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (digits.charAt(i) - '0') * (length + 1 - i);
        }
        int remainder = (sum * 10) % 11;
        return remainder == 10 ? 0 : remainder;
    }
}
