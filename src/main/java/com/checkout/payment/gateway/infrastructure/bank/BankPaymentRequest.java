package com.checkout.payment.gateway.infrastructure.bank;

import com.fasterxml.jackson.annotation.JsonProperty;

record BankPaymentRequest(
    @JsonProperty("card_number") String cardNumber,
    @JsonProperty("expiry_date") String expiryDate,
    @JsonProperty("currency") String currency,
    @JsonProperty("amount") Integer amount,
    @JsonProperty("cvv") String cvv) {
}