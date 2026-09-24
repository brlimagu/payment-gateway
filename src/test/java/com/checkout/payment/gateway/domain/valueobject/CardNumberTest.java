package com.checkout.payment.gateway.domain.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.checkout.payment.gateway.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CardNumberTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "12345678901234",       // 14 digits
      "123456789012345",      // 15 digits
      "1234567890123456789"   // 19 digits
  })
  void acceptsFourteenToNineteenDigits(String value) {
    assertEquals(value, new CardNumber(value).value());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "1234567890123",          // 13 digits
      "12345678901234567890",   // 20 digits
      "1234abcd56789012",       // letters
      "2222 4053 4324 8877",    // spaces
      "2222-4053-4324-8877",    // hifens
      ""                        // empty
  })
  void rejectsInvalidCardNumbers(String value) {
    assertThrows(DomainException.class, () -> new CardNumber(value));
  }

  @Test
  void rejectsNull() {
    assertThrows(DomainException.class, () -> new CardNumber(null));
  }

  @Test
  void exposesLastFourDigits() {
    assertEquals("8877", new CardNumber("2222405343248877").lastFour());
  }

  @Test
  void keepsLeadingZeroInLastFour() {
    assertEquals("0123", new CardNumber("22224053430123").lastFour());
  }

  @Test
  void neverPrintsTheFullNumber() {
    CardNumber card = new CardNumber("2222405343248877");
    assertFalse(card.toString().contains("2222405343248877"));
    assertEquals("CardNumber{****8877}", card.toString());
  }
}