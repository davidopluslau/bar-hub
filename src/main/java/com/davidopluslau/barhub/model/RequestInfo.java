package com.davidopluslau.barhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.norberg.automatter.AutoMatter;

/**
 * A team.
 */
@AutoMatter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public interface RequestInfo {

  //CHECKSTYLE:OFF Javadoc
  Integer page();

  @JsonProperty("page_size")
  Integer pageSize();

  //  String order();

  Integer count();

  @JsonProperty("total_count")
  Integer totalCount();

  @JsonProperty("total_pages")
  Integer totalPages();
  //CHECKSTYLE:ON Javadoc
}
