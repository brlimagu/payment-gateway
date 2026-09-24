package com.checkout.payment.gateway.domain.valueobject;

import com.checkout.payment.gateway.domain.enums.Currency;
import com.checkout.payment.gateway.domain.exception.DomainException;

public record Money(Integer amount, Currency currency) {

  public Money {
    if (amount == null) {
      throw new DomainException("Amount is required");
    }
    if (amount <= 0) {
      throw new DomainException("Amount must be greater than zero");
    }
    if (currency == null) {
      throw new DomainException("Currency is required");
    }
  }
}