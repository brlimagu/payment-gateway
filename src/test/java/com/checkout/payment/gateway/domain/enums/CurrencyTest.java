package com.checkout.payment.gateway.domain.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.checkout.payment.gateway.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CurrencyTest {

  @ParameterizedTest
  @ValueSource(strings = {"GBP", "USD", "BRL"})
  void parsesSupportedCurrencies(String code) {
    assertEquals(Currency.valueOf(code), Currency.fromCode(code));
  }

  @ParameterizedTest
  @ValueSource(strings = {"gbp", "Usd", "bRl"})
  void isCaseInsensitive(String code) {
    assertEquals(Currency.valueOf(code.toUpperCase()), Currency.fromCode(code));
  }

  @ParameterizedTest
  @ValueSource(strings = {"JPY", "EUR", "XXX"})
  void rejectsUnsupportedCurrencies(String code) {
    assertThrows(DomainException.class, () -> Currency.fromCode(code));
  }

  @ParameterizedTest
  @ValueSource(strings = {"GB", "GBPP", ""})
  void rejectsWrongLength(String code) {
    assertThrows(DomainException.class, () -> Currency.fromCode(code));
  }

  @Test
  void rejectsNull() {
    assertThrows(DomainException.class, () -> Currency.fromCode(null));
  }
}