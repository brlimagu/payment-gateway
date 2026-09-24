package com.checkout.payment.gateway.domain.exception;

public class DomainException extends RuntimeException {

  public DomainException(String message) {
    super(message);
  }
}