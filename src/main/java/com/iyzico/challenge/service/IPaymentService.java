package com.iyzico.challenge.service;

import com.iyzico.challenge.entity.Payment;
import com.iyzico.challenge.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class IPaymentService {

    private final Logger logger = LoggerFactory.getLogger(IPaymentService.class);
    private final BankService bankService;
    private final PaymentRepository paymentRepository;
    private final OutboxService outboxService;



    public void pay(BigDecimal price) {
        try {
            // Attempt payment with the bank
            BankPaymentRequest request = new BankPaymentRequest();
            request.setPrice(price);
            BankPaymentResponse response = bankService.pay(request);

            // If the response is successful, save the payment to the database
            if (response != null && "200".equals(response.getResultCode())) {
                Payment payment = new Payment();
                payment.setPrice(price);
                payment.setBankResponse(response.getResultCode());
                paymentRepository.save(payment);
                logger.info("Payment saved successfully!");
            }
        } catch (Exception e) {
            // On failure, log the error and save the payment request to MongoDB for retry
            logger.error("Payment failed, saving to outbox for retry. Price: {}", price, e);
            handlePaymentFailure(price);

        }
    }
    public void handlePaymentFailure(BigDecimal price) {
        try {
            // Ödeme başarısız olduğunda MongoDB'ye kayıt ekle
            outboxService.savePaymentToOutboxAsync(price);
        } catch (Exception e) {
            logger.error("Failed to save outbox message to MongoDB for failed payment. Price: {}", price, e);

        }
    }

}

