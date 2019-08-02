package com.davidopluslau.barhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.norberg.automatter.AutoMatter;
import java.util.List;

/**
 * An list of results.
 */
@AutoMatter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface ListResults<T> {

  //CHECKSTYLE:OFF Javadoc
  RequestInfo info();

  List<T> results();
  //CHECKSTYLE:ON Javadoc
}
