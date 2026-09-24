package com.checkout.payment.gateway.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.checkout.payment.gateway.application.command.PaymentResult;
import com.checkout.payment.gateway.application.command.ProcessPaymentCommand;
import com.checkout.payment.gateway.application.exception.BankUnavailableException;
import com.checkout.payment.gateway.application.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.application.port.AcquiringBankClient;
import com.checkout.payment.gateway.application.port.AuthorizationRequest;
import com.checkout.payment.gateway.application.port.BankAuthorization;
import com.checkout.payment.gateway.domain.entity.Payment;
import com.checkout.payment.gateway.domain.enums.Currency;
import com.checkout.payment.gateway.domain.enums.PaymentStatus;
import com.checkout.payment.gateway.domain.exception.DomainException;
import com.checkout.payment.gateway.domain.repository.PaymentsRepository;
import com.checkout.payment.gateway.domain.valueobject.CardNumber;
import com.checkout.payment.gateway.domain.valueobject.ExpiryDate;
import com.checkout.payment.gateway.domain.valueobject.Money;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PaymentGatewayServiceTest {

  private PaymentsRepository repository;
  private AcquiringBankClient bankClient;
  private PaymentGatewayService service;

  @BeforeEach
  void setUp() {
    repository = mock(PaymentsRepository.class);
    bankClient = mock(AcquiringBankClient.class);
    service = new PaymentGatewayService(repository, bankClient);
  }

  private static ProcessPaymentCommand validCommand() {
    return new ProcessPaymentCommand("2222405343248877", 12, 2035, "GBP", 100, "123");
  }

  @Test
  void returnsAuthorizedWhenBankAuthorizes() {
    when(bankClient.authorize(any())).thenReturn(new BankAuthorization(true, "auth-123"));

    PaymentResult result = service.process(validCommand());

    assertEquals(PaymentStatus.AUTHORIZED, result.status());
  }

  @Test
  void returnsDeclinedWhenBankDeclines() {
    when(bankClient.authorize(any())).thenReturn(new BankAuthorization(false, null));

    assertEquals(PaymentStatus.DECLINED, service.process(validCommand()).status());
  }

  @Test
  void returnsOnlyTheLastFourDigitsAndTheOriginalDetails() {
    when(bankClient.authorize(any())).thenReturn(new BankAuthorization(true, "auth-123"));

    PaymentResult result = service.process(validCommand());

    assertEquals("8877", result.cardNumberLastFour());
    assertEquals(12, result.expiryMonth());
    assertEquals(2035, result.expiryYear());
    assertEquals("GBP", result.currency());
    assertEquals(100, result.amount());
  }

  @Test
  void sendsTheFullCardNumberToTheBank() {
    when(bankClient.authorize(any())).thenReturn(new BankAuthorization(true, "auth-123"));

    service.process(validCommand());

    verify(bankClient).authorize(new AuthorizationRequest(
        new CardNumber("2222405343248877"),
        new ExpiryDate(12, 2035),
        new Money(100, Currency.GBP),
        new com.checkout.payment.gateway.domain.valueobject.Cvv("123")));
  }

  @Test
  void storesAuthorizedPayments() {
    when(bankClient.authorize(any())).thenReturn(new BankAuthorization(true, "auth-123"));

    service.process(validCommand());

    verify(repository).add(any(Payment.class));
  }

  @Test
  void storesDeclinedPaymentsToo() {
    when(bankClient.authorize(any())).thenReturn(new BankAuthorization(false, null));

    service.process(validCommand());

    verify(repository).add(any(Payment.class));
  }

  @ParameterizedTest
  @CsvSource({
      "1234567890123,     12, 2035, GBP, 100, 123",
      "2222405343248877,  13, 2035, GBP, 100, 123",
      "2222405343248877,  12, 2020, GBP, 100, 123",
      "2222405343248877,  12, 2035, JPY, 100, 123",
      "2222405343248877,  12, 2035, GBP,   0, 123",
      "2222405343248877,  12, 2035, GBP, 100,  12"
  })
  void rejectsInvalidPaymentsWithoutCallingTheBankOrStoringAnything(
      String card, Integer month, Integer year, String currency, Integer amount, String cvv) {

    ProcessPaymentCommand command =
        new ProcessPaymentCommand(card, month, year, currency, amount, cvv);

    assertThrows(DomainException.class, () -> service.process(command));

    verifyNoInteractions(bankClient);
    verifyNoInteractions(repository);
  }

  @Test
  void doesNotStoreAnythingWhenTheBankIsUnavailable() {
    when(bankClient.authorize(any()))
        .thenThrow(new BankUnavailableException("bank down", null));

    assertThrows(BankUnavailableException.class, () -> service.process(validCommand()));

    verifyNoInteractions(repository);
  }

  @Test
  void retrievesAnExistingPayment() {
    Payment payment = Payment.create(
        PaymentStatus.AUTHORIZED,
        new CardNumber("2222405343248877"),
        new ExpiryDate(12, 2035),
        new Money(100, Currency.GBP));
    when(repository.findById(payment.getId())).thenReturn(Optional.of(payment));

    PaymentResult result = service.getById(payment.getId());

    assertEquals(payment.getId(), result.id());
    assertEquals("8877", result.cardNumberLastFour());
  }

  @Test
  void throwsWhenPaymentDoesNotExist() {
    when(repository.findById(any())).thenReturn(Optional.empty());

    assertThrows(PaymentNotFoundException.class, () -> service.getById(UUID.randomUUID()));
  }
}