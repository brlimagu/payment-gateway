package com.checkout.payment.gateway.infrastructure.bank;

import com.checkout.payment.gateway.application.exception.BankContractException;
import com.checkout.payment.gateway.application.exception.BankUnavailableException;
import com.checkout.payment.gateway.application.port.AcquiringBankClient;
import com.checkout.payment.gateway.application.port.AuthorizationRequest;
import com.checkout.payment.gateway.application.port.BankAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.ConnectException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
public class RestAcquiringBankClient implements AcquiringBankClient {

  private static final Logger LOG = LoggerFactory.getLogger(RestAcquiringBankClient.class);

  private final RestTemplate restTemplate;
  private final String paymentsUri;

  public RestAcquiringBankClient(RestTemplate restTemplate,
      @Value("${acquiring-bank.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.paymentsUri = baseUrl + "/payments";
  }

  @Override
  @Retryable(
      retryFor = RetryableBankException.class,
      notRecoverable = {BankUnavailableException.class, BankContractException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 300, multiplier = 2))
  public BankAuthorization authorize(AuthorizationRequest request) {
    BankPaymentRequest body = toBankRequest(request);

    try {
      BankPaymentResponse response =
          restTemplate.postForObject(paymentsUri, body, BankPaymentResponse.class);

      if (response == null) {
        throw new BankUnavailableException("Acquiring bank returned an empty body", null);
      }
      return new BankAuthorization(response.authorized(), response.authorizationCode());

    } catch (HttpServerErrorException e) {
      LOG.error("Acquiring bank returned {}", e.getStatusCode());
      throw new BankUnavailableException("Acquiring bank is unavailable", e);

    } catch (HttpClientErrorException e) {
      LOG.error("Acquiring bank rejected our request with {} - this is a gateway bug",
          e.getStatusCode());
      throw new BankContractException("Invalid request sent to acquiring bank", e);

    } catch (ResourceAccessException e) {
      if (isSafeToRetry(e)) {
        LOG.warn("Could not reach acquiring bank ({}), will retry",
            e.getCause().getClass().getSimpleName());
        throw new RetryableBankException("Acquiring bank is unreachable", e);
      }
      LOG.error("Acquiring bank call failed with unknown outcome, not retrying", e);
      throw new BankUnavailableException("Acquiring bank is unreachable", e);
    }
  }

  private static BankPaymentRequest toBankRequest(AuthorizationRequest request) {
    return new BankPaymentRequest(
        request.cardNumber().value(),
        request.expiryDate().toBankFormat(),
        request.money().currency().name(),
        request.money().amount(),
        request.cvv().value());
  }

  @Recover
  BankAuthorization recoverFromUnreachableBank(RetryableBankException e,
      AuthorizationRequest request) {
    LOG.error("Acquiring bank unreachable after {} attempts", 3, e);
    throw new BankUnavailableException("Acquiring bank is unreachable", e);
  }

  private static boolean isSafeToRetry(ResourceAccessException e) {
    Throwable cause = e.getCause();
    return cause instanceof ConnectException
        || cause instanceof UnknownHostException
        || cause instanceof SSLException;
  }
}