package com.davidopluslau.barhub.handler;


import static com.davidopluslau.barhub.db.generated.Tables.COMPONENT_TABLE;
import static com.davidopluslau.barhub.db.generated.Tables.DRINK_COMPONENT_TABLE;
import static com.davidopluslau.barhub.db.generated.Tables.DRINK_TABLE;
import static com.davidopluslau.barhub.db.generated.Tables.MENU_DRINK_TABLE;
import static com.davidopluslau.barhub.db.generated.Tables.MENU_TABLE;

import com.davidopluslau.barhub.db.MenuUtils;
import com.davidopluslau.barhub.db.generated.enums.UnitType;
import com.davidopluslau.barhub.db.generated.tables.pojos.Component;
import com.davidopluslau.barhub.db.generated.tables.pojos.DrinkComponent;
import com.davidopluslau.barhub.db.generated.tables.records.DrinkComponentRecord;
import com.davidopluslau.barhub.db.generated.tables.records.DrinkRecord;
import com.davidopluslau.barhub.db.generated.tables.records.MenuDrinkRecord;
import com.davidopluslau.barhub.db.pojos.Drink;
import com.davidopluslau.barhub.model.ListResults;
import com.davidopluslau.barhub.model.ListResultsBuilder;
import com.davidopluslau.barhub.model.RequestInfoBuilder;
import com.networknt.body.BodyHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

class DrinkHandlers {

  private static final BigDecimal ML_PER_OUNCE = new BigDecimal("30");
  private static final BigDecimal ML_PER_BARSPOON = new BigDecimal("3");
  private static final BigDecimal CENTS_PER_DOLLAR = new BigDecimal("100");

  private DrinkHandlers() {
  }

  public static class GetMenuDrinks extends AbstractHandler implements HttpHandler {

    GetMenuDrinks() {
      super();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

      final String currentMenuVersion = MenuUtils.getCurrentMenuVersion();
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

  public static class Insert extends AbstractHandler implements HttpHandler {

    Insert() {
      super();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      Map<String, Object> queryParameters = (Map<String, Object>) exchange.getAttachment(BodyHandler.REQUEST_BODY);
      ensureParams(queryParameters, List.of("name", "version", "blurb", "isPour", "position", "drinkComponents"));
      String drinkName = (String) queryParameters.get("name");
      String version = (String) queryParameters.get("version");
      AtomicInteger position = new AtomicInteger();
      List<Map<String, Object>> drinkComponentArgs = (List<Map<String, Object>>) queryParameters.get("drinkComponents");
      List<DrinkComponentRecord> drinkComponents = drinkComponentArgs.stream()
          .map(drinkComponent -> {
            ensureParams(drinkComponent, List.of("name", "displayUnits", "displayUnitType"));
            String componentName = (String) drinkComponent.get("name");
            Component component = db.dsl().selectFrom(COMPONENT_TABLE)
                .where(COMPONENT_TABLE.NAME.eq(componentName))
                .fetchOneInto(Component.class);
            BigDecimal displayUnits = new BigDecimal((String) drinkComponent.get("displayUnits"));
            UnitType unitType = UnitType.valueOf((String) drinkComponent.get("displayUnitType"));
            BigDecimal mL = toMl(displayUnits, unitType);
            BigDecimal componentUnitCost = component.getUnitCost();
            BigDecimal costContribution = mL.multiply(componentUnitCost)
                .divide(CENTS_PER_DOLLAR)
                .setScale(2, RoundingMode.HALF_UP);
            BigDecimal alcoholPercentage = component.getAlcoholPercentage();
            BigDecimal alcoholContribution = alcoholPercentage.divide(CENTS_PER_DOLLAR).multiply(mL)
                .setScale(2, RoundingMode.HALF_UP);
            return new DrinkComponentRecord(drinkName, version, componentName, mL, componentUnitCost, costContribution,
                alcoholPercentage, alcoholContribution, displayUnits, unitType, position.getAndIncrement());
          })
          .collect(Collectors.toList());
      BigDecimal drinkCost = drinkComponents.stream()
          .map(DrinkComponentRecord::getCostContribution)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      BigDecimal drinkAlcohol = drinkComponents.stream()
          .map(DrinkComponentRecord::getAlcoholContribution)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      String blurb = (String) queryParameters.get("blurb");
      boolean isPour = (boolean) queryParameters.get("isPour");
      DrinkRecord drink = new DrinkRecord(drinkName, version, drinkCost, drinkAlcohol, blurb, isPour);

      int menuPosition = (int) queryParameters.get("position");
      List<MenuDrinkRecord> menuDrinks = db.dsl().selectFrom(MENU_DRINK_TABLE)
          .where(MENU_DRINK_TABLE.MENU_VERSION.eq(version))
          .and(MENU_DRINK_TABLE.POSITION.greaterOrEqual(menuPosition))
          .orderBy(MENU_DRINK_TABLE.POSITION.asc())
          .fetchInto(MenuDrinkRecord.class);
      menuDrinks.forEach(menuDrink -> menuDrink.setPosition(menuDrink.getPosition() + 1));
      MenuDrinkRecord menuDrink = new MenuDrinkRecord(version, drinkName, version, menuPosition);
      final List<DrinkRecord> result = new ArrayList<>();
      db.dsl().transaction((outer) -> {
        final DSLContext transaction = DSL.using(outer);
        result.add(transaction.insertInto(DRINK_TABLE).set(drink).returning().fetchOne());
        transaction.batchInsert(drinkComponents).execute();
        transaction.batchUpdate(menuDrinks).execute();
        transaction.executeInsert(menuDrink);
        transaction.update(MENU_TABLE)
            .set(MENU_TABLE.N_DRINKS, menuPosition + menuDrinks.size() + 1)
            .where(MENU_TABLE.VERSION.eq(version)).execute();
      });

      sendResponse(exchange, result.get(0).into(new Drink()));
    }

    private BigDecimal toMl(BigDecimal displayUnits, UnitType unitType) {
      switch (unitType) {
        case mL:
          return displayUnits;
        case oz:
          return displayUnits.multiply(ML_PER_OUNCE).setScale(0, RoundingMode.HALF_UP);
        case barspoon:
          return displayUnits.multiply(ML_PER_BARSPOON);
        case dash:
          return BigDecimal.ONE;
        default:
          throw new RuntimeException("I really should support this");
      }
    }
  }
}
