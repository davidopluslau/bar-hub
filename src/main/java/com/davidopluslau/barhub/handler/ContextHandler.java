package com.davidopluslau.barhub.handler;

import com.davidopluslau.barhub.db.MenuUtils;
import com.davidopluslau.barhub.db.generated.enums.UnitType;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.Map;

class ContextHandler {

  static class Get extends AbstractHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

      Map<String, Object> context = Map.ofEntries(
          Map.entry("unitTypes", UnitType.values()),
          Map.entry("menuVersion", MenuUtils.getCurrentMenuVersion())
      );
      sendResponse(exchange, context);
    }
  }

}
