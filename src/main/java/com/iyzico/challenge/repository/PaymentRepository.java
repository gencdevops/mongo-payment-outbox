package com.iyzico.challenge.repository;

import com.iyzico.challenge.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
