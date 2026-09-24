package com.checkout.payment.gateway.infrastructure.bank;

import com.fasterxml.jackson.annotation.JsonProperty;

record BankPaymentResponse(
    @JsonProperty("authorized") boolean authorized,
    @JsonProperty("authorization_code") String authorizationCode) {
}