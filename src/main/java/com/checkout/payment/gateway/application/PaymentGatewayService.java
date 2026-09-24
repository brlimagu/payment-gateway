package com.checkout.payment.gateway.application;

import com.checkout.payment.gateway.application.command.PaymentResult;
import com.checkout.payment.gateway.application.command.ProcessPaymentCommand;
import com.checkout.payment.gateway.application.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.application.port.AcquiringBankClient;
import com.checkout.payment.gateway.application.port.AuthorizationRequest;
import com.checkout.payment.gateway.application.port.BankAuthorization;
import com.checkout.payment.gateway.domain.entity.Payment;
import com.checkout.payment.gateway.domain.enums.Currency;
import com.checkout.payment.gateway.domain.enums.PaymentStatus;
import com.checkout.payment.gateway.domain.repository.PaymentsRepository;
import com.checkout.payment.gateway.domain.valueobject.CardNumber;
import com.checkout.payment.gateway.domain.valueobject.Cvv;
import com.checkout.payment.gateway.domain.valueobject.ExpiryDate;
import com.checkout.payment.gateway.domain.valueobject.Money;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final AcquiringBankClient acquiringBankClient;

  public PaymentGatewayService(PaymentsRepository paymentsRepository,
      AcquiringBankClient acquiringBankClient) {
    this.paymentsRepository = paymentsRepository;
    this.acquiringBankClient = acquiringBankClient;
  }

  public PaymentResult process(ProcessPaymentCommand command) {
    Currency currency = Currency.fromCode(command.currency());
    CardNumber cardNumber = new CardNumber(command.cardNumber());
    ExpiryDate expiryDate = new ExpiryDate(command.expiryMonth(), command.expiryYear());
    Money money = new Money(command.amount(), currency);
    Cvv cvv = new Cvv(command.cvv());

    BankAuthorization authorization = acquiringBankClient.authorize(
        new AuthorizationRequest(cardNumber, expiryDate, money, cvv));

    PaymentStatus status =
        authorization.authorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED;

    Payment payment = Payment.create(status, cardNumber, expiryDate, money);
    paymentsRepository.add(payment);

    LOG.info("Payment {} finished with status {}", payment.getId(), status);
    return PaymentResult.from(payment);
  }

  public PaymentResult getById(UUID id) {
    LOG.debug("Retrieving payment with id {}", id);
    return paymentsRepository.findById(id)
        .map(PaymentResult::from)
        .orElseThrow(() -> new PaymentNotFoundException(id));
  }
}