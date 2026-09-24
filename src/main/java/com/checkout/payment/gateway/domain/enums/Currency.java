package com.checkout.payment.gateway.domain.enums;

import com.checkout.payment.gateway.domain.exception.DomainException;
import java.util.Locale;

public enum Currency {

  GBP,
  USD,
  BRL;

  public static Currency fromCode(String code) {
    if (code == null || code.length() != 3) {
      throw new DomainException("Currency must be a 3 letter ISO code");
    }
    try {
      return Currency.valueOf(code.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new DomainException("Currency must be one of: GBP, USD or BRL");
    }
  }
}