package com.checkout.payment.gateway.infrastructure.bank;

class RetryableBankException extends RuntimeException {

  RetryableBankException(String message, Throwable cause) {
    super(message, cause);
  }
}