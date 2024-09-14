package com.iyzico.challenge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iyzico.challenge.entity.OutboxMessage;
import com.iyzico.challenge.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;


    @Async
    public void savePaymentToOutboxAsync(BigDecimal price){
        savePaymentToOutbox(price);
    }

    private void savePaymentToOutbox(BigDecimal price) {
        try {
            // Generate a unique request ID
            String requestId = UUID.randomUUID().toString();

            // Create and save an outbox message to MongoDB
            OutboxMessage outboxMessage = createOutboxMessage(price, requestId);
            outboxRepository.save(outboxMessage);
            log.info("Outbox message saved to MongoDB, requestId: {}", requestId);
        } catch (Exception e) {
            log.error("Failed to save outbox message to MongoDB.", e);
            throw new RuntimeException("Failed to save outbox message to MongoDB.", e);
        }
    }

    private OutboxMessage createOutboxMessage(BigDecimal price, String requestId) {
        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setRequestId(requestId);
        outboxMessage.setEventType("PAYMENT");
        outboxMessage.setPayload(createPaymentPayload(price));
        outboxMessage.setCreatedAt(LocalDateTime.now());
        outboxMessage.setStatus("PENDING");
        return outboxMessage;
    }

    private String createPaymentPayload(BigDecimal price) {
        try {
            BankPaymentRequest request = new BankPaymentRequest();
            request.setPrice(price);
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to create payment payload.", e);
        }
    }

    public List<OutboxMessage> findPendingMessages() {
        return outboxRepository.findByStatus("PENDING");
    }

    public void saveOutboxMessage(OutboxMessage message) {
        try {
            outboxRepository.save(message);
        } catch (Exception e) {
            log.error("Failed to update outbox message in MongoDB.", e);
        }
    }

}