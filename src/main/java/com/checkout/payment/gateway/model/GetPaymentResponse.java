package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.application.command.PaymentResult;
import com.checkout.payment.gateway.domain.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public record GetPaymentResponse(
    @JsonProperty("id") UUID id,
    @JsonProperty("status") PaymentStatus status,
    @JsonProperty("card_number_last_four") String cardNumberLastFour,
    @JsonProperty("expiry_month") Integer expiryMonth,
    @JsonProperty("expiry_year") Integer expiryYear,
    @JsonProperty("currency") String currency,
    @JsonProperty("amount") Integer amount) {

  public static GetPaymentResponse from(PaymentResult result) {
    return new GetPaymentResponse(
        result.id(),
        result.status(),
        result.cardNumberLastFour(),
        result.expiryMonth(),
        result.expiryYear(),
        result.currency(),
        result.amount());
  }
}