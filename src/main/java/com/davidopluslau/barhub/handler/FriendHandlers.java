package com.davidopluslau.barhub.handler;


import static com.davidopluslau.barhub.db.generated.Tables.FRIEND_TABLE;

import com.davidopluslau.barhub.db.generated.tables.pojos.Friend;
import com.davidopluslau.barhub.db.generated.tables.records.FriendRecord;
import com.davidopluslau.barhub.model.ListResults;
import com.davidopluslau.barhub.model.ListResultsBuilder;
import com.davidopluslau.barhub.model.RequestInfoBuilder;
import com.networknt.body.BodyHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.List;
import java.util.Map;

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

  public static class Insert extends AbstractHandler implements HttpHandler {

    Insert() {
      super();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

      Map<String, Object> queryParameters = (Map<String, Object>) exchange.getAttachment(BodyHandler.REQUEST_BODY);
      ensureParams(queryParameters, List.of("name"));

      final Friend friend = db.dsl().insertInto(FRIEND_TABLE)
          .set(new FriendRecord(queryParameters.get("name").toString(), null)).returning().fetchOne()
          .into(Friend.class);

      sendResponse(exchange, friend);
    }
  }
}
