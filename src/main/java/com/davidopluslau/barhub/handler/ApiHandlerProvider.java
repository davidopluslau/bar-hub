package com.davidopluslau.barhub.handler;

import com.networknt.config.Config;
import com.networknt.handler.HandlerProvider;
import com.networknt.status.Status;
import com.networknt.status.exception.ApiException;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;

/**
 * Handler provider.
 */
public class ApiHandlerProvider implements HandlerProvider {

  @Override
  public HttpHandler getHandler() {

    return Handlers.routing()
        // Drink
        .add(Methods.GET, "/drink", new DrinkHandlers.GetMenuDrinks())
        .add(Methods.GET, "/drink/{id}", new DrinkHandlers.GetCurrent())
        .add(Methods.POST, "/drink", new DrinkHandlers.Insert())
        .add(Methods.GET, "/component", new ComponentHandlers.GetAll())
        .add(Methods.POST, "/component", new ComponentHandlers.Insert())
        .add(Methods.GET, "/context", new ContextHandler.Get())
        .add(Methods.GET, "/friend", new FriendHandlers.GetAll())
        ;
  }

  private static class NotImplementedHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      throw new ApiException(new Status("ERR20501"));
    }
  }

  private static class StaticFileHandler implements HttpHandler {

    private final String filename;
    private final String contentType;

    private StaticFileHandler(final String filename, final String contentType) {
      this.filename = filename;
      this.contentType = contentType;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      final String payload = Config.getInstance().getStringFromFile(filename);
      exchange.getResponseHeaders().add(new HttpString("Content-Type"), contentType);
      exchange.getResponseSender().send(payload);
    }
  }
}
