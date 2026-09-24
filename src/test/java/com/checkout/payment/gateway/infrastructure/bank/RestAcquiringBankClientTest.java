package com.checkout.payment.gateway.infrastructure.bank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.checkout.payment.gateway.application.exception.BankContractException;
import com.checkout.payment.gateway.application.exception.BankUnavailableException;
import com.checkout.payment.gateway.application.port.AuthorizationRequest;
import com.checkout.payment.gateway.application.port.BankAuthorization;
import com.checkout.payment.gateway.application.port.AcquiringBankClient;
import com.checkout.payment.gateway.configuration.ApplicationConfiguration;
import com.checkout.payment.gateway.domain.enums.Currency;
import com.checkout.payment.gateway.domain.valueobject.CardNumber;
import com.checkout.payment.gateway.domain.valueobject.Cvv;
import com.checkout.payment.gateway.domain.valueobject.ExpiryDate;
import com.checkout.payment.gateway.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(RestAcquiringBankClient.class)
@Import(ApplicationConfiguration.class)
@TestPropertySource(properties = {
    "acquiring-bank.url=http://bank",
    "acquiring-bank.connect-timeout-ms=2000",
    "acquiring-bank.read-timeout-ms=5000"
})
class RestAcquiringBankClientTest {

  @Autowired
  private AcquiringBankClient client;

  @Autowired
  private MockRestServiceServer server;

  private static AuthorizationRequest request() {
    return new AuthorizationRequest(
        new CardNumber("2222405343248877"),
        new ExpiryDate(4, 2035),
        new Money(100, Currency.GBP),
        new Cvv("123"));
  }

  @Test
  void sendsExactlyThePayloadTheBankExpects() {
    server.expect(requestTo("http://bank/payments"))
        .andExpect(method(HttpMethod.POST))
        .andExpect(jsonPath("$.card_number").value("2222405343248877"))
        .andExpect(jsonPath("$.expiry_date").value("04/2035"))
        .andExpect(jsonPath("$.currency").value("GBP"))
        .andExpect(jsonPath("$.amount").value(100))
        .andExpect(jsonPath("$.cvv").value("123"))
        .andRespond(withSuccess(
            "{\"authorized\":true,\"authorization_code\":\"abc-123\"}",
            MediaType.APPLICATION_JSON));

    BankAuthorization authorization = client.authorize(request());

    assertTrue(authorization.authorized());
    assertEquals("abc-123", authorization.authorizationCode());
    server.verify();
  }

  @Test
  void mapsNotAuthorizedResponse() {
    server.expect(requestTo("http://bank/payments"))
        .andRespond(withSuccess(
            "{\"authorized\":false,\"authorization_code\":\"\"}",
            MediaType.APPLICATION_JSON));

    assertEquals(false, client.authorize(request()).authorized());
  }

  @Test
  void mapsServiceUnavailableToBankUnavailable() {
    server.expect(requestTo("http://bank/payments"))
        .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

    assertThrows(BankUnavailableException.class, () -> client.authorize(request()));
  }

  @Test
  void mapsInternalServerErrorToBankUnavailable() {
    server.expect(requestTo("http://bank/payments"))
        .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

    assertThrows(BankUnavailableException.class, () -> client.authorize(request()));
  }

  @Test
  void mapsBadRequestToContractException() {
    server.expect(requestTo("http://bank/payments"))
        .andRespond(withStatus(HttpStatus.BAD_REQUEST));

    assertThrows(BankContractException.class, () -> client.authorize(request()));
  }
}