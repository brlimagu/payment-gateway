package com.checkout.payment.gateway.application.command;

public record ProcessPaymentCommand(String cardNumber, Integer expiryMonth, Integer expiryYear,
                                    String currency, Integer amount, String cvv) {

  @Override
  public String toString() {
    return "ProcessPaymentCommand{card=****, expiryMonth=" + expiryMonth
        + ", expiryYear=" + expiryYear + ", currency=" + currency
        + ", amount=" + amount + "}";
  }
}