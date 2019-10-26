package com.davidopluslau.barhub.db.pojos;

import java.util.ArrayList;
import java.util.List;

public class OrderResponse extends com.davidopluslau.barhub.db.generated.tables.pojos.Order {
  private final List<OrderDrinkResponse> orderDrinks = new ArrayList<>();

  public List<OrderDrinkResponse> getOrderDrinks() {
    return orderDrinks;
  }

  public void setOrderDrinks(List<OrderDrinkResponse> orderDrinks) {
    this.orderDrinks.addAll(orderDrinks);
  }
}
