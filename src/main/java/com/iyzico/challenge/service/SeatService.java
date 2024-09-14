package com.iyzico.challenge.service;


import com.iyzico.challenge.entity.Flight;
import com.iyzico.challenge.entity.Seat;
import com.iyzico.challenge.repository.FlightRepository;
import com.iyzico.challenge.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final SeatRepository seatRepository;
    private final FlightRepository flightRepository;

    // Koltuk bilgilerini al ve kilitle
    @Transactional(readOnly = true)
    public Optional<Seat> getSeatById(Long id) {
        return Optional.ofNullable(seatRepository.findByIdForUpdate(id));
    }

    // Koltuk bilgilerini güncelle
    @Transactional
    public void updateSeat(Long seatId, Seat updatedSeat) {
        Seat existingSeat = seatRepository.findByIdForUpdate(seatId);
        if (existingSeat == null) {
            throw new RuntimeException("Seat not found");
        }
        existingSeat.setSeatNumber(updatedSeat.getSeatNumber());
        existingSeat.setPrice(updatedSeat.getPrice());
        existingSeat.setAvailable(updatedSeat.isAvailable());
        seatRepository.save(existingSeat);
    }

    @Transactional
    public Seat addSeat(Long flightId, Seat seat) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found"));
        seat.setFlight(flight);
        seat.setAvailable(true);
        return seatRepository.save(seat);
    }

    @Transactional
    public void deleteSeat(Long seatId) {
        seatRepository.deleteById(seatId);
    }

    public List<Seat> getSeatsByFlight(Long flightId) {
        return seatRepository.findByFlightId(flightId);
    }

    public boolean isSeatAvailable(Long seatId) {
        Optional<Seat> seat = seatRepository.findById(seatId);
        return seat.isPresent() && seat.get().isAvailable();
    }
}
