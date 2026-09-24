package com.checkout.payment.gateway.domain.valueobject;

import com.checkout.payment.gateway.domain.exception.DomainException;
import java.time.YearMonth;

public record ExpiryDate(Integer month, Integer year) {

  public static final int MIN_MONTH = 1;
  public static final int MAX_MONTH = 12;
  public static final int MAX_YEAR = 9999;

  public ExpiryDate {
    YearMonth now = YearMonth.now();

    if (month == null) {
      throw new DomainException("Expiry month is required");
    }
    if (month < MIN_MONTH || month > MAX_MONTH) {
      throw new DomainException("Expiry month must be between 1 and 12");
    }
    if (year == null) {
      throw new DomainException("Expiry year is required");
    }
    if (year > MAX_YEAR) {
      throw new DomainException("Expiry year must have at most four digits");
    }
    if (year < now.getYear()) {
      throw new DomainException("Card expiry date must be in the future");
    }
    if (YearMonth.of(year, month).isBefore(now)) {
      throw new DomainException("Card expiry date must be in the future");
    }
  }

  public String toBankFormat() {
    return String.format("%02d/%d", month, year);
  }
}