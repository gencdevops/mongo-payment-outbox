package com.iyzico.challenge.exception;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;



public class SeatNotFoundException extends RuntimeException {
    public SeatNotFoundException(String message) {
        super(message);
    }
}
