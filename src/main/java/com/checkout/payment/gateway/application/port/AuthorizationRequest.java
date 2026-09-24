package com.checkout.payment.gateway.application.port;

import com.checkout.payment.gateway.domain.valueobject.CardNumber;
import com.checkout.payment.gateway.domain.valueobject.Cvv;
import com.checkout.payment.gateway.domain.valueobject.ExpiryDate;
import com.checkout.payment.gateway.domain.valueobject.Money;

public record AuthorizationRequest(CardNumber cardNumber, ExpiryDate expiryDate,
                                   Money money, Cvv cvv) {
}