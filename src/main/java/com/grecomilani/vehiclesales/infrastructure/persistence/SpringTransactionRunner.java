package com.grecomilani.vehiclesales.infrastructure.persistence;

import com.grecomilani.vehiclesales.application.port.TransactionRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
class SpringTransactionRunner implements TransactionRunner {

    private final TransactionTemplate transactionTemplate;

    SpringTransactionRunner(PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public <T> T inTransaction(Supplier<T> operation) {
        return transactionTemplate.execute(status -> operation.get());
    }
}
