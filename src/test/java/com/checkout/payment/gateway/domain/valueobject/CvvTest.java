package com.checkout.payment.gateway.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.checkout.payment.gateway.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CvvTest {

  @ParameterizedTest
  @ValueSource(strings = {"123", "1234"})
  void acceptsThreeOrFourDigits(String value) {
    assertEquals(value, new Cvv(value).value());
  }

  @Test
  void preservesLeadingZeros() {
    assertEquals("012", new Cvv("012").value());
    assertEquals("0001", new Cvv("0001").value());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "12",     // 2 digits
      "12345",  // 5 digits
      "abc",    // not a number
      "12a",    // alphanumeric
      ""        // empty
  })
  void rejectsInvalidCvv(String value) {
    assertThrows(DomainException.class, () -> new Cvv(value));
  }

  @Test
  void rejectsNull() {
    assertThrows(DomainException.class, () -> new Cvv(null));
  }

  @Test
  void neverPrintsTheValue() {
    Cvv cvv = new Cvv("123");
    assertFalse(cvv.toString().contains("123"));
    assertEquals("Cvv{***}", cvv.toString());
  }
}