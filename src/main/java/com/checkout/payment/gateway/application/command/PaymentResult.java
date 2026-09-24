package com.checkout.payment.gateway.application.command;

import com.checkout.payment.gateway.domain.entity.Payment;
import com.checkout.payment.gateway.domain.enums.PaymentStatus;
import java.util.UUID;

public record PaymentResult(UUID id, PaymentStatus status, String cardNumberLastFour,
                            Integer expiryMonth, Integer expiryYear,
                            String currency, Integer amount) {

  public static PaymentResult from(Payment payment) {
    return new PaymentResult(
        payment.getId(),
        payment.getStatus(),
        payment.getCardNumberLastFour(),
        payment.getExpiryDate().month(),
        payment.getExpiryDate().year(),
        payment.getMoney().currency().name(),
        payment.getMoney().amount());
  }
}