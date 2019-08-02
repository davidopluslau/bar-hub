package com.davidopluslau.barhub.db.converters;

import java.sql.Date;
import java.time.ZoneId;
import java.util.GregorianCalendar;
import org.jetbrains.annotations.Nullable;
import org.jooq.Converter;

/**
 * Converts Dates into Calendar dates.
 */
public class CalendarConverter implements Converter<Date, GregorianCalendar> {

  private static final long serialVersionUID = 1L;

  @Override
  public GregorianCalendar from(@Nullable Date databaseObject) {
    if (databaseObject == null) {
      return null;
    }
    return GregorianCalendar.from(databaseObject.toLocalDate().atStartOfDay(ZoneId.systemDefault()));
  }

  @Override
  public Date to(@Nullable GregorianCalendar userObject) {
    return userObject == null ? null : Date.valueOf(userObject.toZonedDateTime().toLocalDate());
  }

  @Override
  public Class<Date> fromType() {
    return Date.class;
  }

  @Override
  public Class<GregorianCalendar> toType() {
    return GregorianCalendar.class;
  }
}
