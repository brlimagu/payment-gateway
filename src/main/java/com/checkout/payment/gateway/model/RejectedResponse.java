package com.checkout.payment.gateway.model;

import com.checkout.payment.gateway.domain.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record RejectedResponse(
    @JsonProperty("status") PaymentStatus status,
    @JsonProperty("reasons") List<String> reasons) {

  public static RejectedResponse of(String reason) {
    return new RejectedResponse(PaymentStatus.REJECTED, List.of(reason));
  }

  public static RejectedResponse of(List<String> reasons) {
    return new RejectedResponse(PaymentStatus.REJECTED, reasons);
  }
}