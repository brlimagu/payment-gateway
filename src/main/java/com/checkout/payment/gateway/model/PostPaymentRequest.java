package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.application.command.ProcessPaymentCommand;
import com.checkout.payment.gateway.domain.enums.Currency;
import com.checkout.payment.gateway.domain.valueobject.CardNumber;
import com.checkout.payment.gateway.domain.valueobject.Cvv;
import com.checkout.payment.gateway.domain.valueobject.ExpiryDate;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record PostPaymentRequest(

    @JsonProperty("card_number")
    @NotNull(message = "card_number is required")
    @Pattern(regexp = CardNumber.PATTERN,
        message = "card_number must be between 14 and 19 numeric characters")
    String cardNumber,

    @JsonProperty("expiry_month")
    @NotNull(message = "expiry_month is required")
    @Min(value = ExpiryDate.MIN_MONTH, message = "expiry_month must be between 1 and 12")
    @Max(value = ExpiryDate.MAX_MONTH, message = "expiry_month must be between 1 and 12")
    Integer expiryMonth,

    @JsonProperty("expiry_year")
    @NotNull(message = "expiry_year is required")
    @Max(value = ExpiryDate.MAX_YEAR, message = "expiry_year must have at most four digits")
    Integer expiryYear,

    @JsonProperty("currency")
    @NotNull(message = "currency is required")
    @Pattern(regexp = Currency.CODE_PATTERN, message = "currency must be a 3 letter ISO code")
    String currency,

    @JsonProperty("amount")
    @NotNull(message = "amount is required")
    @Positive(message = "amount must be greater than zero")
    Integer amount,

    @JsonProperty("cvv")
    @NotNull(message = "cvv is required")
    @Pattern(regexp = Cvv.PATTERN, message = "cvv must be 3 or 4 numeric characters")
    String cvv) {

  public ProcessPaymentCommand toCommand() {
    return new ProcessPaymentCommand(cardNumber, expiryMonth, expiryYear, currency, amount, cvv);
  }

  @Override
  public String toString() {
    return "PostPaymentRequest{card=****, expiryMonth=" + expiryMonth
        + ", expiryYear=" + expiryYear + ", currency=" + currency
        + ", amount=" + amount + "}";
  }
}