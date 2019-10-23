package com.davidopluslau.barhub.db;

import static com.davidopluslau.barhub.db.generated.Tables.MENU_TABLE;

import com.networknt.service.SingletonServiceFactory;

public class MenuUtils {

  private static DatabaseProvider db;

  public MenuUtils() {
  }

  public static String getCurrentMenuVersion() {
    return getDb().dsl().select(MENU_TABLE.VERSION)
        .from(MENU_TABLE)
        .orderBy(MENU_TABLE.VERSION.desc())
        .limit(1)
        .fetchOneInto(String.class);
  }

  private static DatabaseProvider getDb() {
    if (db == null) {
      db = SingletonServiceFactory.getBean(DatabaseProvider.class);
    }
    return db;
  }
}
