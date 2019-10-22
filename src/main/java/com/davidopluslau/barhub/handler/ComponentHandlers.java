package com.davidopluslau.barhub.handler;


import static com.davidopluslau.barhub.db.generated.Tables.COMPONENT_TABLE;

import com.davidopluslau.barhub.db.generated.enums.UnitType;
import com.davidopluslau.barhub.db.generated.tables.pojos.Component;
import com.davidopluslau.barhub.db.generated.tables.records.ComponentRecord;
import com.davidopluslau.barhub.model.ListResults;
import com.davidopluslau.barhub.model.ListResultsBuilder;
import com.davidopluslau.barhub.model.RequestInfoBuilder;
import com.networknt.body.BodyHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

public class ComponentHandlers {

  private static final BigDecimal CENTS_PER_DOLLAR = new BigDecimal("100.00");

  private ComponentHandlers() {
  }

  public static class GetAll extends AbstractHandler implements HttpHandler {

    GetAll() {
      super();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

      List<Component> components = db.dsl().selectFrom(COMPONENT_TABLE)
          .orderBy(COMPONENT_TABLE.NAME)
          .fetchInto(Component.class);

      final ListResults<Component> componentResult = new ListResultsBuilder<Component>()
          .results(components)
          .info(new RequestInfoBuilder()
              .pageSize(components.size())
              .page(1)
              .count(components.size())
              .totalCount(components.size())
              .totalPages(1)
              .build()).build();

      sendResponse(exchange, componentResult);
    }
  }

  public static class Insert extends AbstractHandler implements HttpHandler {

    Insert() {
      super();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
      Map<String, Object> queryParameters = (Map<String, Object>) exchange.getAttachment(BodyHandler.REQUEST_BODY);
      ensureParams(queryParameters,
          List.of("name", "singleCost", "taxMultiplier", "singleSize", "unitType", "alcoholPercentage", "isPour"));
      String name = (String) queryParameters.get("name");
      BigDecimal singleCost = new BigDecimal((String) queryParameters.get("singleCost"));
      BigDecimal taxMultiplier = new BigDecimal((String) queryParameters.get("taxMultiplier"));
      BigDecimal adjustedSingleCost = singleCost.multiply(taxMultiplier).setScale(2, RoundingMode.HALF_UP);
      int singleSize = (int) queryParameters.get("singleSize");
      UnitType unitType = UnitType.valueOf((String) queryParameters.get("unitType"));
      BigDecimal unitCost = adjustedSingleCost.multiply(CENTS_PER_DOLLAR)
          .divide(BigDecimal.valueOf(singleSize), RoundingMode.HALF_UP)
          .setScale(4, RoundingMode.HALF_UP);
      BigDecimal alcoholPercentage = new BigDecimal((String) queryParameters.get("alcoholPercentage"));
      boolean isPour = (boolean) queryParameters.get("isPour");
      String site = (String) queryParameters.get("site");
      String notes = (String) queryParameters.get("notes");
      ComponentRecord component = new ComponentRecord(name, singleCost, adjustedSingleCost, singleSize, unitType,
          unitCost, alcoholPercentage, taxMultiplier, isPour, site, notes);

      Component result = db.dsl().insertInto(COMPONENT_TABLE).set(component).onConflictDoNothing().returning()
          .fetchOne().into(new Component());
      sendResponse(exchange, result);
    }
  }
}
