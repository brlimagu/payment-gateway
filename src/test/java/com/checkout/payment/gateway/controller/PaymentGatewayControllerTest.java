package com.checkout.payment.gateway.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.checkout.payment.gateway.application.exception.BankUnavailableException;
import com.checkout.payment.gateway.application.port.AcquiringBankClient;
import com.checkout.payment.gateway.application.port.BankAuthorization;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerTest {

  private static final String VALID_BODY = """
      {"card_number":"2222405343248877","expiry_month":4,"expiry_year":2035,
       "currency":"GBP","amount":100,"cvv":"123"}
      """;

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AcquiringBankClient bankClient;

  @Test
  void authorizedPaymentReturnsOkAndNeverLeaksCardData() throws Exception {
    when(bankClient.authorize(any())).thenReturn(new BankAuthorization(true, "abc"));

    mvc.perform(post("/payment").contentType(APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.card_number_last_four").value("8877"))
        .andExpect(jsonPath("$.amount").value(100))
        .andExpect(jsonPath("$.card_number").doesNotExist())
        .andExpect(jsonPath("$.cvv").doesNotExist());
  }

  @Test
  void declinedPaymentReturnsOkWithDeclinedStatus() throws Exception {
    when(bankClient.authorize(any())).thenReturn(new BankAuthorization(false, null));

    mvc.perform(post("/payment").contentType(APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Declined"));
  }

  @Test
  void declinedPaymentIsRetrievable() throws Exception {
    when(bankClient.authorize(any())).thenReturn(new BankAuthorization(false, null));

    MvcResult created = mvc.perform(
            post("/payment").contentType(APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isOk())
        .andReturn();

    JsonNode body = objectMapper.readTree(created.getResponse().getContentAsString());
    String id = body.get("id").asText();

    mvc.perform(get("/payment/" + id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Declined"));
  }

  @Test
  void expiredCardIsRejectedWithReasonAndNeverReachesTheBank() throws Exception {
    String expired = VALID_BODY.replace("\"expiry_year\":2035", "\"expiry_year\":2020");

    mvc.perform(post("/payment").contentType(APPLICATION_JSON).content(expired))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value("Rejected"))
        .andExpect(jsonPath("$.reasons", hasSize(1)))
        .andExpect(jsonPath("$.reasons[0]").value(containsString("future")));

    verifyNoInteractions(bankClient);
  }

  @Test
  void collectsEveryEdgeViolationInOneResponse() throws Exception {
    String allWrong = "{\"card_number\":\"123\",\"expiry_month\":4,\"expiry_year\":2035,"
        + "\"currency\":\"GBP\",\"amount\":0,\"cvv\":\"1\"}";

    mvc.perform(post("/payment").contentType(APPLICATION_JSON).content(allWrong))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value("Rejected"))
        .andExpect(jsonPath("$.reasons", hasSize(3)))
        .andExpect(jsonPath("$.reasons[0]").value(containsString("amount")))
        .andExpect(jsonPath("$.reasons[1]").value(containsString("card_number")))
        .andExpect(jsonPath("$.reasons[2]").value(containsString("cvv")));

    verifyNoInteractions(bankClient);
  }

  @Test
  void missingFieldsAreAllReportedAtOnce() throws Exception {
    mvc.perform(post("/payment").contentType(APPLICATION_JSON).content("{}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value("Rejected"))
        .andExpect(jsonPath("$.reasons", hasSize(6)));
  }

  @Test
  void monthThirteenIsRejectedNotServerError() throws Exception {
    String badMonth = VALID_BODY.replace("\"expiry_month\":4", "\"expiry_month\":13");

    mvc.perform(post("/payment").contentType(APPLICATION_JSON).content(badMonth))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value("Rejected"));
  }

  @Test
  void unsupportedCurrencyIsRejected() throws Exception {
    String jpy = VALID_BODY.replace("\"currency\":\"GBP\"", "\"currency\":\"JPY\"");

    mvc.perform(post("/payment").contentType(APPLICATION_JSON).content(jpy))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value("Rejected"));

    verifyNoInteractions(bankClient);
  }

  @Test
  void decimalAmountIsRejectedAsMalformedBody() throws Exception {
    String decimal = VALID_BODY.replace("\"amount\":100", "\"amount\":10.50");

    mvc.perform(post("/payment").contentType(APPLICATION_JSON).content(decimal))
        .andExpect(status().isBadRequest());

    verifyNoInteractions(bankClient);
  }

  @Test
  void nonNumericAmountIsRejectedAsMalformedBody() throws Exception {
    String text = VALID_BODY.replace("\"amount\":100", "\"amount\":\"dez\"");

    mvc.perform(post("/payment").contentType(APPLICATION_JSON).content(text))
        .andExpect(status().isBadRequest());
  }

  @Test
  void bankOutageReturnsBadGateway() throws Exception {
    when(bankClient.authorize(any()))
        .thenThrow(new BankUnavailableException("bank down", null));

    mvc.perform(post("/payment").contentType(APPLICATION_JSON).content(VALID_BODY))
        .andExpect(status().isBadGateway());
  }

  @Test
  void unknownPaymentIdReturnsNotFound() throws Exception {
    mvc.perform(get("/payment/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Payment not found"));
  }

  @Test
  void malformedPaymentIdReturnsBadRequest() throws Exception {
    mvc.perform(get("/payment/not-a-uuid"))
        .andExpect(status().isBadRequest());
  }
}