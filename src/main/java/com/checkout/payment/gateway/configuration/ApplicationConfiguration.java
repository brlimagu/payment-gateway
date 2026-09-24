package com.checkout.payment.gateway.configuration;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableRetry
public class ApplicationConfiguration {

  @Bean
  public RestTemplate restTemplate(
      RestTemplateBuilder builder,
      @Value("${acquiring-bank.connect-timeout-ms}") long connectTimeoutMs,
      @Value("${acquiring-bank.read-timeout-ms}") long readTimeoutMs) {
    return builder
        .setConnectTimeout(Duration.ofMillis(connectTimeoutMs))
        .setReadTimeout(Duration.ofMillis(readTimeoutMs))
        .build();
  }
}