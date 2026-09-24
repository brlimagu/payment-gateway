package com.checkout.payment.gateway.application.command;

public record ProcessPaymentCommand(String cardNumber, Integer expiryMonth, Integer expiryYear,
                                    String currency, Integer amount, String cvv) {
}