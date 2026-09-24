package com.checkout.payment.gateway.infrastructure.persistence;

import com.checkout.payment.gateway.domain.entity.Payment;
import com.checkout.payment.gateway.domain.repository.PaymentsRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryPaymentsRepository implements PaymentsRepository {

  private final Map<UUID, Payment> payments = new ConcurrentHashMap<>();

  @Override
  public void add(Payment payment) {
    payments.put(payment.getId(), payment);
  }

  @Override
  public Optional<Payment> findById(UUID id) {
    return Optional.ofNullable(payments.get(id));
  }
}