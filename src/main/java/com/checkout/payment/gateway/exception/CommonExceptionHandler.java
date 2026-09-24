package com.checkout.payment.gateway.exception;

import com.checkout.payment.gateway.application.exception.BankContractException;
import com.checkout.payment.gateway.application.exception.BankUnavailableException;
import com.checkout.payment.gateway.application.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.domain.exception.DomainException;
import com.checkout.payment.gateway.model.ErrorResponse;
import com.checkout.payment.gateway.model.RejectedResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class CommonExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<RejectedResponse> handleInvalidRequest(
      MethodArgumentNotValidException ex) {
    List<String> reasons = ex.getBindingResult().getAllErrors().stream()
        .map(DefaultMessageSourceResolvable::getDefaultMessage)
        .sorted()
        .toList();
    LOG.info("Payment rejected at the edge: {}", reasons);
    return ResponseEntity.unprocessableEntity().body(RejectedResponse.of(reasons));
  }

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<RejectedResponse> handleDomainException(DomainException ex) {
    LOG.info("Payment rejected: {}", ex.getMessage());
    return ResponseEntity.unprocessableEntity()
        .body(RejectedResponse.of(ex.getMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
    LOG.info("Malformed request body: {}", ex.getMessage());
    return ResponseEntity.badRequest()
        .body(new ErrorResponse("Malformed request body"));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    LOG.info("Invalid path parameter: {}", ex.getName());
    return ResponseEntity.badRequest()
        .body(new ErrorResponse("Invalid payment id"));
  }

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ErrorResponse("Payment not found"));
  }

  @ExceptionHandler(BankUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleBankUnavailable(BankUnavailableException ex) {
    LOG.error("Acquiring bank unavailable", ex);
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(new ErrorResponse("Payment could not be processed, please retry later"));
  }

  @ExceptionHandler(BankContractException.class)
  public ResponseEntity<ErrorResponse> handleBankContract(BankContractException ex) {
    LOG.error("Gateway sent an invalid request to the acquiring bank", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("Unexpected error"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
    LOG.error("Unexpected error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("Unexpected error"));
  }
}