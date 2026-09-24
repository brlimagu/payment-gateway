package com.checkout.payment.gateway.domain.valueobject;

import com.checkout.payment.gateway.domain.exception.DomainException;
import java.util.regex.Pattern;

public record Cvv(String value) {

  public static final String PATTERN = "\\d{3,4}";

  private static final Pattern VALID = Pattern.compile(PATTERN);

  public Cvv {
    if (value == null || !VALID.matcher(value).matches()) {
      throw new DomainException("CVV must be 3 or 4 numeric characters");
    }
  }

  @Override
  public String toString() {
    return "Cvv{***}";
  }
}