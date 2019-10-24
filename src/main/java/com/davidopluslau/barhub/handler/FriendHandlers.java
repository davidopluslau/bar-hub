package com.davidopluslau.barhub.handler;


import com.davidopluslau.barhub.db.generated.tables.pojos.Friend;
import com.davidopluslau.barhub.model.ListResults;
import com.davidopluslau.barhub.model.ListResultsBuilder;
import com.davidopluslau.barhub.model.RequestInfoBuilder;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.List;

import static com.davidopluslau.barhub.db.generated.Tables.FRIEND_TABLE;

public class FriendHandlers {

  private FriendHandlers() {
  }

  public static class GetAll extends AbstractHandler implements HttpHandler {

    public GetAll() {
      super();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

      final List<Friend> friends = db.dsl().selectFrom(FRIEND_TABLE)
          .fetchInto(Friend.class);

      final ListResults<Friend> friendResults = new ListResultsBuilder<Friend>()
          .results(friends)
          .info(new RequestInfoBuilder()
              .pageSize(friends.size())
              .page(1)
              .count(friends.size())
              .totalCount(friends.size())
              .totalPages(1)
              .build()).build();

      sendResponse(exchange, friendResults);
    }
  }

//  public static class GetCurrent extends AbstractHandler implements HttpHandler {
//
//    GetCurrent() {
//      super();
//    }
//
//    @Override
//    public void handleRequest(HttpServerExchange exchange) throws Exception {
//
//      final String gid = exchange.getQueryParameters().get("id").getFirst();
//
//      final Drink drink = db.dsl().selectFrom(DRINK_TABLE)
//          .where(DRINK_TABLE.NAME.eq(gid))
//          .orderBy(DRINK_TABLE.VERSION.desc())
//          .limit(1)
//          .fetchOneInto(Drink.class);
//
//      final List<DrinkComponent> drinkComponents = db.dsl().selectFrom(DRINK_COMPONENT_TABLE)
//          .where(DRINK_COMPONENT_TABLE.DRINK_NAME.eq(drink.getName()))
//          .and(DRINK_COMPONENT_TABLE.DRINK_VERSION.eq(drink.getVersion()))
//          .orderBy(DRINK_COMPONENT_TABLE.POSITION.asc())
//          .fetchInto(DrinkComponent.class);
//      drink.getDrinkComponents().addAll(drinkComponents);
//
//      sendResponse(exchange, drink);
//    }
//  }
}
