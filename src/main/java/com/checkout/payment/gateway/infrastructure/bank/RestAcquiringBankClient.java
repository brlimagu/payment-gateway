package com.checkout.payment.gateway.infrastructure.bank;

import com.checkout.payment.gateway.application.exception.BankContractException;
import com.checkout.payment.gateway.application.exception.BankUnavailableException;
import com.checkout.payment.gateway.application.port.AcquiringBankClient;
import com.checkout.payment.gateway.application.port.AuthorizationRequest;
import com.checkout.payment.gateway.application.port.BankAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
      LOG.error("Could not reach acquiring bank", e);
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
}