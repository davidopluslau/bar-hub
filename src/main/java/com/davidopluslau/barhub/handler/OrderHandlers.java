package com.davidopluslau.barhub.handler;

import static com.davidopluslau.barhub.db.generated.Tables.DRINK_TABLE;
import static com.davidopluslau.barhub.db.generated.Tables.FRIEND_TABLE;
import static com.davidopluslau.barhub.db.generated.Tables.ORDER_DRINK_TABLE;
import static com.davidopluslau.barhub.db.generated.Tables.ORDER_TABLE;

import com.davidopluslau.barhub.db.generated.tables.pojos.Friend;
import com.davidopluslau.barhub.db.generated.tables.pojos.Order;
import com.davidopluslau.barhub.db.generated.tables.records.DrinkRecord;
import com.davidopluslau.barhub.db.generated.tables.records.OrderDrinkRecord;
import com.davidopluslau.barhub.db.generated.tables.records.OrderRecord;
import com.davidopluslau.barhub.db.pojos.OrderDrinkResponse;
import com.davidopluslau.barhub.db.pojos.OrderResponse;
import com.davidopluslau.barhub.model.ListResults;
import com.davidopluslau.barhub.model.ListResultsBuilder;
import com.davidopluslau.barhub.model.RequestInfoBuilder;
import com.networknt.body.BodyHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

class OrderHandlers {

  private OrderHandlers() {
  }

  public static class GetAll extends AbstractHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      List<Order> orders = db.dsl().selectFrom(ORDER_TABLE)
          .orderBy(ORDER_TABLE.ORDER_DATE.desc())
          .fetchInto(Order.class);

      final ListResults<Order> orderResult = new ListResultsBuilder<Order>()
          .results(orders)
          .info(new RequestInfoBuilder()
              .pageSize(orders.size())
              .page(1)
              .count(orders.size())
              .totalCount(orders.size())
              .totalPages(1)
              .build()).build();

      sendResponse(exchange, orderResult);
    }
  }

  public static class Insert extends AbstractHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      Map<String, Object> queryParameters = (Map<String, Object>) exchange.getAttachment(BodyHandler.REQUEST_BODY);
      ensureParams(queryParameters, List.of("friendName", "menuVersion", "date", "orderDrinks"));

      Friend friend = db.dsl().selectFrom(FRIEND_TABLE)
          .where(FRIEND_TABLE.NAME.eq(queryParameters.get("friendName").toString()))
          .fetchOneInto(Friend.class);
      final String name = friend.getName();
      final String menuVersion = queryParameters.get("menuVersion").toString();

      final LocalDate date = LocalDate.parse(queryParameters.get("date").toString());
      List<Map<String, Object>> drinks = (List<Map<String, Object>>) queryParameters.get("orderDrinks");
      AtomicInteger position = new AtomicInteger();
      AtomicBoolean isFirstDrink = new AtomicBoolean(friend.getFirstDrinkDate() == null);
      final List<OrderDrinkRecord> orderDrinks = drinks.stream()
          .map(orderDrink -> {
            ensureParams(orderDrink, List.of("name", "drinkVersion", "quantity"));
            String drinkName = orderDrink.get("name").toString();
            String drinkVersion = orderDrink.get("drinkVersion").toString();
            final BigDecimal quantity = new BigDecimal(orderDrink.get("quantity").toString());
            final DrinkRecord drink = db.dsl().selectFrom(DRINK_TABLE).where(DRINK_TABLE.NAME.eq(drinkName))
                .fetchOneInto(DrinkRecord.class);
            BigDecimal drinkCost = drink.getCost().multiply(quantity).setScale(2, RoundingMode.HALF_UP);
            final BigDecimal discount;
            if (orderDrink.containsKey("discount")) {
              discount = new BigDecimal(orderDrink.get("discount").toString());
            } else if (isFirstDrink.get()) {
              discount = drinkCost;
              friend.setFirstDrinkDate(date);
            } else {
              discount = BigDecimal.ZERO.setScale(2);
            }
            return new OrderDrinkRecord(date, name, drinkName, drinkVersion, quantity.setScale(2, RoundingMode.HALF_UP),
                menuVersion, drinkCost, discount, isFirstDrink.getAndSet(false), position.getAndIncrement());
          })
          .collect(Collectors.toList());
      final BigDecimal orderCost = orderDrinks.stream()
          .map(OrderDrinkRecord::getCost)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      final BigDecimal orderDiscount = orderDrinks.stream()
          .map(OrderDrinkRecord::getDiscount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      final OrderRecord order = new OrderRecord(name, menuVersion, orderCost, orderDiscount,
          orderCost.subtract(orderDiscount),
          date, (String) queryParameters.get("notes"));

      final List<OrderResponse> result = new ArrayList<>();
      db.dsl().transaction((outer) -> {
        final DSLContext transaction = DSL.using(outer);
        result.add(transaction.insertInto(ORDER_TABLE).set(order).returning().fetchOne().into(new OrderResponse()));
        transaction.batchInsert(orderDrinks).execute();
        final List<OrderDrinkResponse> orderDrinkResponses = transaction.selectFrom(ORDER_DRINK_TABLE)
            .where(ORDER_DRINK_TABLE.FRIEND_NAME.eq(name))
            .and(ORDER_DRINK_TABLE.ORDER_DATE.eq(date))
            .fetchInto(OrderDrinkResponse.class);
        result.get(0).setOrderDrinks(orderDrinkResponses);
      });

      sendResponse(exchange, result.get(0));
    }
  }
}
