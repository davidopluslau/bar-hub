package com.davidopluslau.barhub.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BarHubConfigProvider {
  private static final Logger LOG = LoggerFactory.getLogger(BarHubConfigProvider.class);

  private static final String SERVICE_NAME = "bar-hub";
  private final Config config;

  /**
   * Constructor.
   */
  public BarHubConfigProvider() {
    config = ConfigFactory.parseResources(String.format("config/%s.conf", SERVICE_NAME)).resolve();
  }

  /**
   * Get The HOCON ConfigTree.
   *
   * @return the ConfigTree
   */
  public Config getConfig() {
    return config;
  }
}
