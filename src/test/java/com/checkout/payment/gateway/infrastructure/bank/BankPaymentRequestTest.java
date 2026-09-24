package com.checkout.payment.gateway.infrastructure.bank;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class BankPaymentRequestTest {

  @Test
  void neverPrintsCardNumberOrCvv() {
    BankPaymentRequest request =
        new BankPaymentRequest("2222405343248877", "04/2035", "USD", 100, "7391");

    assertFalse(request.toString().contains("2222405343248877"));
    assertFalse(request.toString().contains("7391"));
  }
}