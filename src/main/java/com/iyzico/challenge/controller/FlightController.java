package com.iyzico.challenge.controller;

import com.iyzico.challenge.entity.Flight;
import com.iyzico.challenge.entity.Seat;
import com.iyzico.challenge.service.FlightService;
import com.iyzico.challenge.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;
    private final SeatService seatService;

    // Flight ekleme
    @PostMapping
    public ResponseEntity<Flight> addFlight(@RequestBody Flight flight) {
        Flight savedFlight = flightService.addFlight(flight);
        return ResponseEntity.ok(savedFlight);
    }

    // Flight güncelleme
    @PutMapping("/{id}")
    public ResponseEntity<Flight> updateFlight(@PathVariable Long id, @RequestBody Flight flight) {
        Flight updatedFlight = flightService.updateFlight(id, flight);
        return ResponseEntity.ok(updatedFlight);
    }

    // Flight silme
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }

    // Tüm flight'ları listeleme
    @GetMapping
    public ResponseEntity<List<Flight>> listAllFlights() {
        List<Flight> flights = flightService.listAllFlights();
        return ResponseEntity.ok(flights);
    }

    // Bir uçuşa ait tüm koltukları listeleme
    @GetMapping("/{flightId}/seats")
    public ResponseEntity<List<Seat>> getSeatsByFlight(@PathVariable Long flightId) {
        List<Seat> seats = flightService.getSeatsByFlight(flightId);
        return ResponseEntity.ok(seats);
    }

    // Koltuk satın alma (ödeme işlemi)
    @PostMapping("/seats/{seatId}/purchase")
    public ResponseEntity<Void> purchaseSeat(@PathVariable Long seatId, @RequestParam BigDecimal price) {
        try {
            flightService.processPayment(seatId, price);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
