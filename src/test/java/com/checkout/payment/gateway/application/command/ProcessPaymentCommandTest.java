package com.checkout.payment.gateway.application.command;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class ProcessPaymentCommandTest {

  @Test
  void neverPrintsCardNumberOrCvv() {
    ProcessPaymentCommand command =
        new ProcessPaymentCommand("2222405343248877", 4, 2035, "BRL", 100, "7391");

    assertFalse(command.toString().contains("2222405343248877"));
    assertFalse(command.toString().contains("7391"));
  }
}