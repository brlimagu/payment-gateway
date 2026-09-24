package com.checkout.payment.gateway.infrastructure.bank;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.checkout.payment.gateway.application.exception.BankUnavailableException;
import com.checkout.payment.gateway.application.port.AcquiringBankClient;
import com.checkout.payment.gateway.application.port.AuthorizationRequest;
import com.checkout.payment.gateway.domain.enums.Currency;
import com.checkout.payment.gateway.domain.valueobject.CardNumber;
import com.checkout.payment.gateway.domain.valueobject.Cvv;
import com.checkout.payment.gateway.domain.valueobject.ExpiryDate;
import com.checkout.payment.gateway.domain.valueobject.Money;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Tag("integration")
@SpringBootTest
class BankSimulatorIntegrationTest {

  @Autowired
  private AcquiringBankClient client;

  private static AuthorizationRequest withCard(String cardNumber) {
    return new AuthorizationRequest(
        new CardNumber(cardNumber),
        new ExpiryDate(4, 2035),
        new Money(100, Currency.GBP),
        new Cvv("123"));
  }

  @Test
  void oddLastDigitIsAuthorized() {
    assertTrue(client.authorize(withCard("2222405343248877")).authorized());
  }

  @Test
  void evenLastDigitIsNotAuthorized() {
    assertFalse(client.authorize(withCard("2222405343248872")).authorized());
  }

  @Test
  void lastDigitZeroMeansBankUnavailable() {
    assertThrows(BankUnavailableException.class,
        () -> client.authorize(withCard("2222405343248870")));
  }
}