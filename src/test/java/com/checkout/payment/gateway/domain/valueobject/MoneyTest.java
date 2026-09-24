package com.checkout.payment.gateway.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.checkout.payment.gateway.domain.enums.Currency;
import com.checkout.payment.gateway.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MoneyTest {

  @ParameterizedTest
  @ValueSource(ints = {1, 100, 1050, Integer.MAX_VALUE})
  void acceptsPositiveAmounts(int amount) {
    assertEquals(amount, new Money(amount, Currency.GBP).amount());
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -1050})
  void rejectsNonPositiveAmounts(int amount) {
    DomainException e =
        assertThrows(DomainException.class, () -> new Money(amount, Currency.GBP));
    assertTrue(e.getMessage().contains("greater than zero"));
  }

  @Test
  void rejectsNullAmountWithItsOwnMessage() {
    DomainException e =
        assertThrows(DomainException.class, () -> new Money(null, Currency.GBP));
    assertTrue(e.getMessage().contains("required"));
  }

  @Test
  void rejectsNullCurrency() {
    assertThrows(DomainException.class, () -> new Money(100, null));
  }
}