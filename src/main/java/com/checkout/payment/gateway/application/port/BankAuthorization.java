package com.checkout.payment.gateway.application.port;

public record BankAuthorization(boolean authorized, String authorizationCode) {
}