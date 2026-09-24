package com.checkout.payment.gateway.controller;

import com.checkout.payment.gateway.application.PaymentGatewayService;
import com.checkout.payment.gateway.application.command.PaymentResult;
import com.checkout.payment.gateway.model.GetPaymentResponse;
import com.checkout.payment.gateway.model.PostPaymentRequest;
import com.checkout.payment.gateway.model.PostPaymentResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
    this.paymentGatewayService = paymentGatewayService;
  }

  @PostMapping
  public ResponseEntity<PostPaymentResponse> postPayment(
      @Valid @RequestBody PostPaymentRequest request) {
    PaymentResult result = paymentGatewayService.process(request.toCommand());
    return ResponseEntity.ok(PostPaymentResponse.from(result));
  }

  @GetMapping("/{id}")
  public ResponseEntity<GetPaymentResponse> getPaymentById(@PathVariable UUID id) {
    PaymentResult result = paymentGatewayService.getById(id);
    return ResponseEntity.ok(GetPaymentResponse.from(result));
  }
}