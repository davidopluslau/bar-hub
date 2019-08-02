package com.davidopluslau.barhub.db;

import com.networknt.server.ShutdownHookProvider;
import com.networknt.service.SingletonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shutdown Provider.
 */
public class DatabaseShutdownHook implements ShutdownHookProvider {
  private static final Logger LOG = LoggerFactory.getLogger(DatabaseShutdownHook.class);

  public DatabaseShutdownHook() {
  }

  @Override
  public void onShutdown() {
    LOG.info("Closing Database Connection Pool");
    final DatabaseProvider databaseProvider = SingletonServiceFactory.getBean(DatabaseProvider.class);
    if (databaseProvider != null) {
      databaseProvider.close();
    }
  }
}
