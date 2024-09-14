package com.iyzico.challenge.service;


import com.iyzico.challenge.entity.Flight;
import com.iyzico.challenge.entity.Seat;
import com.iyzico.challenge.exception.SeatNotAvailableException;
import com.iyzico.challenge.exception.SeatNotFoundException;
import com.iyzico.challenge.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final Logger logger = LoggerFactory.getLogger(FlightService.class);
    private final IPaymentService iPaymentService;
    private final FlightRepository flightRepository;
    private final SeatService seatService;
    private final OutboxService outboxService;


    // Flight ekleme servisi
    public Flight addFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    // Flight güncelleme servisi
    public Flight updateFlight(Long id, Flight flight) {
        Flight existingFlight = flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found"));
        existingFlight.setName(flight.getName());
        existingFlight.setDescription(flight.getDescription());
        return flightRepository.save(existingFlight);
    }

    // Flight silme servisi
    public void deleteFlight(Long id) {
        flightRepository.deleteById(id);
    }

    // Tüm flight'ları listeleme servisi
    public List<Flight> listAllFlights() {
        return flightRepository.findAll();
    }

    // Bir uçuşa ait tüm koltukları listeleme servisi
    public List<Seat> getSeatsByFlight(Long flightId) {
        return seatService.getSeatsByFlight(flightId);
    }



    public void processPayment(Long seatId, BigDecimal price) {
        try {
            // Koltuk bilgilerini al ve kilitle
            Seat seat = seatService.getSeatById(seatId)
                    .orElseThrow(() -> new SeatNotFoundException("Seat not found with ID: " + seatId));

            synchronized (seat) {
                if (!seat.isAvailable()) {
                    throw new SeatNotAvailableException("Seat is already sold with ID: " + seatId);
                }

                // Ödeme işlemini gerçekleştir
                iPaymentService.pay(price);

                // Koltuğun durumunu güncelle
                seat.setAvailable(false);
                seatService.updateSeat(seatId, seat);

                logger.info("Payment processed successfully for seat ID: {}", seatId);
            }
        } catch (SeatNotFoundException | SeatNotAvailableException e) {
            logger.error("Error processing payment for seat ID: {} - {}", seatId, e.getMessage());
            throw e;  // Bu tür istisnaları tekrar fırlat
        } catch (Exception e) {
            logger.error("Unexpected error occurred while processing payment for seat ID: {}", seatId, e);
            throw new RuntimeException("Unexpected error occurred during payment processing", e);
        }
    }
}
