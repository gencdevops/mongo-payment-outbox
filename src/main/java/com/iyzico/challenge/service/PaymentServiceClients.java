package com.iyzico.challenge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service

public class PaymentServiceClients {
    @Autowired
    IPaymentService iPaymentService;

    public PaymentServiceClients(IPaymentService iPaymentService) {
        this.iPaymentService = iPaymentService;
    }

    @Async
    public CompletableFuture<String> call(BigDecimal price) {
        iPaymentService.pay(price);
        return CompletableFuture.completedFuture("success");
    }
}
