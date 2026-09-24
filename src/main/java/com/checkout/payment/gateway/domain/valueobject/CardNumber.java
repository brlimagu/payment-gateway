package com.checkout.payment.gateway.domain.valueobject;

import com.checkout.payment.gateway.domain.exception.DomainException;
import java.util.regex.Pattern;

public record CardNumber(String value) {

  public static final String PATTERN = "\\d{14,19}";

  private static final Pattern VALID = Pattern.compile(PATTERN);

  public CardNumber {
    if (value == null || !VALID.matcher(value).matches()) {
      throw new DomainException("Card number must be between 14 and 19 numeric characters");
    }
  }

  public String lastFour() {
    return value.substring(value.length() - 4);
  }

  @Override
  public String toString() {
    return "CardNumber{****" + lastFour() + "}";
  }
}