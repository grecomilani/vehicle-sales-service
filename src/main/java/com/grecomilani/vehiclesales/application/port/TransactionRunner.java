package com.grecomilani.vehiclesales.application.port;

import java.util.function.Supplier;

/**
 * Executa uma operação dentro de uma transação: ou todas as alterações são
 * confirmadas, ou nenhuma é.
 */
public interface TransactionRunner {

    <T> T inTransaction(Supplier<T> operation);
}
