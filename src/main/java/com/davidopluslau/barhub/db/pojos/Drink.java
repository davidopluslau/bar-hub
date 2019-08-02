package com.davidopluslau.barhub.db.pojos;

import com.davidopluslau.barhub.db.generated.tables.pojos.DrinkComponent;
import java.util.ArrayList;
import java.util.List;

public class Drink extends com.davidopluslau.barhub.db.generated.tables.pojos.Drink {
  private final List<DrinkComponent> drinkComponents = new ArrayList<>();

  public List<DrinkComponent> getDrinkComponents() {
    return drinkComponents;
  }
}
