package com.davidopluslau.barhub.handler;

import com.davidopluslau.barhub.config.BarHubConfigProvider;
import com.davidopluslau.barhub.db.DatabaseProvider;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.config.Config;
import com.networknt.service.SingletonServiceFactory;
import com.networknt.status.Status;
import com.networknt.status.exception.ApiException;
import io.norberg.automatter.jackson.AutoMatterModule;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import java.util.concurrent.CompletionStage;

/**
 * Abstract Handler.
 */
public abstract class AbstractHandler {
  private static final ObjectMapper objectMapper;

  private static final String DEFAULT_PAGE = "1";
  private static final String DEFAULT_PAGE_SIZE = "10";

  private final BarHubConfigProvider configProvider;
  protected final DatabaseProvider db;

  static {
    objectMapper = Config.getInstance().getMapper()
        .registerModule(new JavaTimeModule())
        .registerModule(new AutoMatterModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  protected AbstractHandler() {
    configProvider = SingletonServiceFactory.getBean(BarHubConfigProvider.class);
    db = SingletonServiceFactory.getBean(DatabaseProvider.class);
  }

  private <T> String toJsonString(final T payload) {
    try {
      return objectMapper.writer().writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
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

  String getQueryParameter(final HttpServerExchange exchange, final String name, final String alt) {
    if (exchange.getQueryParameters().containsKey(name)) {
      return exchange.getQueryParameters().get(name).getFirst();
    }

    return alt;
  }

  protected Integer getListPage(final HttpServerExchange exchange) {
    return Integer.valueOf(getQueryParameter(exchange, "page", DEFAULT_PAGE));
  }

  protected Integer getListPageSize(final HttpServerExchange exchange) {
    return Integer.valueOf(getQueryParameter(exchange, "page_size", DEFAULT_PAGE_SIZE));
  }

  protected Integer getListOffset(final HttpServerExchange exchange) {
    return (getListPage(exchange) - 1) * getListPageSize(exchange);
  }

  protected Integer getTotalPages(final Integer totalCount, final Integer pageSize) {
    return (int) Math.ceil(((double) totalCount / (double) pageSize));
  }
}

