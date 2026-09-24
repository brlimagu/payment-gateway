package com.checkout.payment.gateway.application.exception;

public class BankContractException extends RuntimeException {

  public BankContractException(String message, Throwable cause) {
    super(message, cause);
  }
}