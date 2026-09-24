package com.checkout.payment.gateway.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.checkout.payment.gateway.domain.exception.DomainException;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ExpiryDateTest {

  @ParameterizedTest
  @ValueSource(ints = {1, 6, 12})
  void acceptsAllValidMonths(int month) {
    assertEquals(month, new ExpiryDate(month, 2035).month());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 13, -1, 99})
  void rejectsMonthOutOfRange(int month) {
    DomainException e = assertThrows(DomainException.class, () -> new ExpiryDate(month, 2035));
    assertTrue(e.getMessage().contains("month"));
  }

  @Test
  void rejectsNullMonthWithItsOwnMessage() {
    DomainException e = assertThrows(DomainException.class, () -> new ExpiryDate(null, 2035));
    assertEquals("Expiry month is required", e.getMessage());
  }

  @Test
  void rejectsNullYearWithItsOwnMessage() {
    DomainException e = assertThrows(DomainException.class, () -> new ExpiryDate(12, null));
    assertEquals("Expiry year is required", e.getMessage());
  }

  @ParameterizedTest
  @ValueSource(ints = {10000, 999999999, Integer.MAX_VALUE})
  void rejectsYearWithMoreThanFourDigits(int year) {
    DomainException e = assertThrows(DomainException.class, () -> new ExpiryDate(6, year));
    assertEquals("Expiry year must have at most four digits", e.getMessage());
  }

  @ParameterizedTest
  @ValueSource(ints = {2020, 999, 0, -1, Integer.MIN_VALUE})
  void rejectsPastYearsWithoutCrashing(int year) {
    DomainException e = assertThrows(DomainException.class, () -> new ExpiryDate(6, year));
    assertEquals("Card expiry date must be in the future", e.getMessage());
  }

  @Test
  void acceptsCardExpiringInTheCurrentMonth() {
    YearMonth now = YearMonth.now();
    assertDoesNotThrow(() -> new ExpiryDate(now.getMonthValue(), now.getYear()));
  }

  @Test
  void acceptsCardExpiringNextMonth() {
    YearMonth next = YearMonth.now().plusMonths(1);
    assertDoesNotThrow(() -> new ExpiryDate(next.getMonthValue(), next.getYear()));
  }

  @Test
  void rejectsCardThatExpiredLastMonth() {
    YearMonth last = YearMonth.now().minusMonths(1);
    DomainException e = assertThrows(DomainException.class,
        () -> new ExpiryDate(last.getMonthValue(), last.getYear()));
    assertTrue(e.getMessage().contains("future"));
  }

  @Test
  void formatsForTheBankWithLeadingZero() {
    assertEquals("04/2035", new ExpiryDate(4, 2035).toBankFormat());
  }

  @Test
  void formatsDoubleDigitMonthWithoutPadding() {
    assertEquals("12/2035", new ExpiryDate(12, 2035).toBankFormat());
  }
}