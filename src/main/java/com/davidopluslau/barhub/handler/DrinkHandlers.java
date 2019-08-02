package com.davidopluslau.barhub.handler;


import static com.davidopluslau.barhub.db.generated.Tables.DRINK_COMPONENT_TABLE;
import static com.davidopluslau.barhub.db.generated.Tables.DRINK_TABLE;
import static com.davidopluslau.barhub.db.generated.Tables.MENU_DRINK_TABLE;
import static com.davidopluslau.barhub.db.generated.Tables.MENU_TABLE;

import com.davidopluslau.barhub.db.generated.tables.pojos.DrinkComponent;
import com.davidopluslau.barhub.db.pojos.Drink;
import com.davidopluslau.barhub.model.ListResults;
import com.davidopluslau.barhub.model.ListResultsBuilder;
import com.davidopluslau.barhub.model.RequestInfoBuilder;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.util.List;

public class DrinkHandlers {

  private DrinkHandlers() {
  }

  public static class GetMenuDrinks extends AbstractHandler implements HttpHandler {

    public GetMenuDrinks() {
      super();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

      final String currentMenuVersion = db.dsl().select(MENU_TABLE.VERSION)
          .from(MENU_TABLE)
          .orderBy(MENU_TABLE.VERSION.desc())
          .limit(1)
          .fetchOneInto(String.class);
      final List<Drink> drinks = db.dsl().select(DRINK_TABLE.fields())
          .from(DRINK_TABLE)
          .join(MENU_DRINK_TABLE)
          .on(DRINK_TABLE.NAME.eq(MENU_DRINK_TABLE.DRINK_NAME))
          .where(MENU_DRINK_TABLE.MENU_VERSION.eq(currentMenuVersion))
          .fetchInto(Drink.class);

      drinks.forEach( // TODO can jOOQ not select from a list of tuples?
          drink -> drink.getDrinkComponents().addAll(
              db.dsl().selectFrom(DRINK_COMPONENT_TABLE)
                  .where(DRINK_COMPONENT_TABLE.DRINK_NAME.eq(drink.getName()))
                  .and(DRINK_COMPONENT_TABLE.DRINK_VERSION.eq(drink.getVersion()))
                  .orderBy(DRINK_COMPONENT_TABLE.POSITION.asc()) // TODO enforce this in database
                  .fetchInto(DrinkComponent.class)
          )
      );

      final ListResults<Drink> drinkResults = new ListResultsBuilder<Drink>()
          .results(drinks)
          .info(new RequestInfoBuilder()
              .pageSize(drinks.size())
              .page(1)
              .count(drinks.size())
              .totalCount(drinks.size())
              .totalPages(1)
              .build()).build();

      sendResponse(exchange, drinkResults);
    }
  }

  /**
   * Retrieve Team Entity.
   */
  public static class GetCurrent extends AbstractHandler implements HttpHandler {

    GetCurrent() {
      super();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

      final String gid = exchange.getQueryParameters().get("id").getFirst();

      final Drink drink = db.dsl().selectFrom(DRINK_TABLE)
          .where(DRINK_TABLE.NAME.eq(gid))
          .orderBy(DRINK_TABLE.VERSION.desc())
          .limit(1)
          .fetchOneInto(Drink.class);

      final List<DrinkComponent> drinkComponents = db.dsl().selectFrom(DRINK_COMPONENT_TABLE)
          .where(DRINK_COMPONENT_TABLE.DRINK_NAME.eq(drink.getName()))
          .and(DRINK_COMPONENT_TABLE.DRINK_VERSION.eq(drink.getVersion()))
          .orderBy(DRINK_COMPONENT_TABLE.POSITION.asc())
          .fetchInto(DrinkComponent.class);
      drink.getDrinkComponents().addAll(drinkComponents);

      sendResponse(exchange, drink);
    }
  }
}
