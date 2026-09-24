package com.checkout.payment.gateway.domain.entity;

import com.checkout.payment.gateway.domain.enums.PaymentStatus;
import com.checkout.payment.gateway.domain.valueobject.CardNumber;
import com.checkout.payment.gateway.domain.valueobject.ExpiryDate;
import com.checkout.payment.gateway.domain.valueobject.Money;
import java.util.UUID;

public final class Payment {

  private final UUID id;
  private final PaymentStatus status;
  private final String cardNumberLastFour;
  private final ExpiryDate expiryDate;
  private final Money money;

  private Payment(UUID id, PaymentStatus status, String cardNumberLastFour,
      ExpiryDate expiryDate, Money money) {
    this.id = id;
    this.status = status;
    this.cardNumberLastFour = cardNumberLastFour;
    this.expiryDate = expiryDate;
    this.money = money;
  }

  public static Payment create(PaymentStatus status, CardNumber cardNumber,
      ExpiryDate expiryDate, Money money) {
    return new Payment(UUID.randomUUID(), status, cardNumber.lastFour(), expiryDate, money);
  }

  public UUID getId() {
    return id;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public String getCardNumberLastFour() {
    return cardNumberLastFour;
  }

  public ExpiryDate getExpiryDate() {
    return expiryDate;
  }

  public Money getMoney() {
    return money;
  }

  @Override
  public String toString() {
    return "Payment{id=" + id + ", status=" + status + ", lastFour=" + cardNumberLastFour + "}";
  }
}