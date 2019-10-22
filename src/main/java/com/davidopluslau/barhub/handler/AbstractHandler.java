package com.davidopluslau.barhub.handler;

import com.davidopluslau.barhub.db.DatabaseProvider;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.config.Config;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import com.networknt.status.exception.ApiException;
import io.norberg.automatter.jackson.AutoMatterModule;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Abstract Handler.
 */
public abstract class AbstractHandler {

  private static final ObjectMapper OBJECT_MAPPER;

  protected final DatabaseProvider db;

  static {
    SimpleModule module = new SimpleModule().addSerializer(NumberSerializer.instance);
    OBJECT_MAPPER = Config.getInstance().getMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new AutoMatterModule())
        .registerModule(module)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  protected AbstractHandler() {
    db = SingletonServiceFactory.getBean(DatabaseProvider.class);
  }

  private <T> String toJsonString(final T payload) {
    try {
      return OBJECT_MAPPER.writer().writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize payload", e);
    }
  }

  protected <T> void sendResponse(final HttpServerExchange exchange, final CompletionStage<T> completionStage)
      throws Exception {
    sendResponse(exchange, completionStage.toCompletableFuture().get());
  }

  <T> void sendResponse(final HttpServerExchange exchange, final T payload)
      throws Exception {
    final String jsonString = toJsonString(payload);
    if (jsonString == null) {
      throw new ApiException(new Status("ERR20040"));
    }
    exchange.getResponseHeaders().add(new HttpString("Content-Type"), "application/json");
    exchange.getResponseSender().send(jsonString);
  }

  String getQueryParameter(final HttpServerExchange exchange, final String name, final String defaultValue) {
    if (exchange.getQueryParameters().containsKey(name)) {
      return exchange.getQueryParameters().get(name).getFirst();
    }

    return defaultValue;
  }

  void ensureParams(Map<String, Object> queryParameters, Collection<String> required) {
    List<String> missingParams = new ArrayList<>();
    for (String param : required) {
      if (!queryParameters.containsKey(param)) {
        missingParams.add(param);
      }
    }
    if (!missingParams.isEmpty()) {
      throw new IllegalArgumentException(
          String.format("Missing values for required arg(s): %s", String.join(", ", missingParams)));
    }
  }

  static BigDecimal parseBigDecimal(String param, Map<String, Deque<String>> queryParameters) {
    String paramValue = queryParameters.get(param).getFirst();
    return new BigDecimal(paramValue);
  }
}

