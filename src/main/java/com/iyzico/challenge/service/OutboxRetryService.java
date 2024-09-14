
package com.iyzico.challenge.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyzico.challenge.entity.OutboxMessage;
import com.iyzico.challenge.entity.Payment;
import com.iyzico.challenge.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxRetryService {

    private final OutboxService outboxService;
    private final BankService bankService;
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;

    @Scheduled(fixedDelay = 10000) // Runs every 10 seconds
    public void retryFailedPayments() {
        List<OutboxMessage> pendingMessages = outboxService.findPendingMessages();

        for (OutboxMessage message : pendingMessages) {
            try {
                BankPaymentRequest request = objectMapper.readValue(message.getPayload(), BankPaymentRequest.class);
                BankPaymentResponse response = bankService.pay(request);

                if (response != null && "200".equals(response.getResultCode())) {
                    Payment payment = new Payment();
                    payment.setPrice(request.getPrice());
                    payment.setBankResponse(response.getResultCode());
                    paymentRepository.save(payment);
                    message.setStatus("COMPLETED");
                } else {
                    throw new RuntimeException("Payment failed with response code: " + (response != null ? response.getResultCode() : "null"));
                }
            } catch (Exception e) {
                log.error("Error processing outbox message with ID: {}", message.getId(), e);

            } finally {
                outboxService.saveOutboxMessage(message);
            }
        }
    }
}
