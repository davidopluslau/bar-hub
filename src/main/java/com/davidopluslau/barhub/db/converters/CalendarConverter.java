package com.davidopluslau.barhub.db.converters;

import java.sql.Date;
import java.time.LocalDate;
import org.jetbrains.annotations.Nullable;
import org.jooq.Converter;

/**
 * Converts Dates into Calendar dates.
 */
public class CalendarConverter implements Converter<Date, LocalDate> {

  private static final long serialVersionUID = 1L;

  @Override
  public LocalDate from(@Nullable Date databaseObject) {
    if (databaseObject == null) {
      return null;
    }
    return databaseObject.toLocalDate();
  }

  @Override
  public Date to(@Nullable LocalDate userObject) {
    return userObject == null ? null : Date.valueOf(userObject);
  }

  @Override
  public Class<Date> fromType() {
    return Date.class;
  }

  @Override
  public Class<LocalDate> toType() {
    return LocalDate.class;
  }
}
