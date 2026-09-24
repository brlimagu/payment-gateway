package com.checkout.payment.gateway.domain.repository;

import com.checkout.payment.gateway.domain.entity.Payment;
import java.util.Optional;
import java.util.UUID;

public interface PaymentsRepository {

  void add(Payment payment);

  Optional<Payment> findById(UUID id);
}