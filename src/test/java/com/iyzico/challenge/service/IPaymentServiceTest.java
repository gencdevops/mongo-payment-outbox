package com.iyzico.challenge.service;

import com.iyzico.challenge.entity.Seat;
import com.iyzico.challenge.repository.PaymentRepository;
import com.iyzico.challenge.repository.SeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnableAutoConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest
@EnableAsync
@Slf4j
public class IPaymentServiceTest {

    @Autowired
    private PaymentServiceClients paymentServiceClients;

    @Autowired
    private FlightService flightService;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private PaymentRepository paymentRepository;




    @Test
    public void should_pay_with_iyzico_with_100_clients_together() {
        List<CompletableFuture> futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            CompletableFuture<String> future = paymentServiceClients.call(new BigDecimal(i));
            futures.add(future);
        }
        futures.stream().forEach(f -> CompletableFuture.allOf(f).join());
    }

    @Test
    public void test_Concurrent_Seat_Purchase() throws InterruptedException, ExecutionException {
        Seat savedSeat = new Seat();
        savedSeat.setSeatNumber("A1");
        savedSeat.setAvailable(true);
        savedSeat.setPrice(BigDecimal.valueOf(100));
        savedSeat = seatRepository.save(savedSeat);

        Long seatId = savedSeat.getId();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            try {
                flightService.processPayment(seatId, BigDecimal.valueOf(100));
            } catch (Exception e) {
                log.error("Exception in future1: {}", e.getMessage());
            }
        }, executorService);

        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
            try {
                flightService.processPayment(seatId, BigDecimal.valueOf(100));
            } catch (Exception e) {
                log.error("Exception in future2: {}", e.getMessage());
            }
        }, executorService);

        CompletableFuture.allOf(future1, future2).join();

        Optional<Seat> seatOptional = seatRepository.findById(seatId);
        Seat seat = seatOptional.orElseThrow(() -> new RuntimeException("Seat not found"));

        assertFalse(seat.isAvailable(), "Seat should not be available");

        long successfulPayments = paymentRepository.count();
        assertEquals(1, successfulPayments, "There should be exactly one successful payment");

        executorService.shutdown();
    }
}
