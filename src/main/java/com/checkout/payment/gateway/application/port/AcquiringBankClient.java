package com.checkout.payment.gateway.application.port;

public interface AcquiringBankClient {

  BankAuthorization authorize(AuthorizationRequest request);
}