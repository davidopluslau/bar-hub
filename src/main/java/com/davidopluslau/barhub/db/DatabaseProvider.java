package com.davidopluslau.barhub.db;

import com.davidopluslau.barhub.config.BarHubConfigProvider;
import com.networknt.service.SingletonServiceFactory;
import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.BackslashEscaping;
import org.jooq.conf.ParamType;
import org.jooq.conf.RenderKeywordStyle;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Database Provider.
 */
public class DatabaseProvider {
  private static final Logger LOG = LoggerFactory.getLogger(DatabaseProvider.class);

  protected final Config config;

  private String username;
  private String password;
  private String url;

  private Map<String, Object> properties;

  private DataSource dataSource;
  protected Configuration configuration;

  private volatile DSLContext dslContext = null;

  /**
   * Constructor.
   */
  public DatabaseProvider() {
    final Config appConfig = SingletonServiceFactory.getBean(BarHubConfigProvider.class).getConfig();
    this.config = appConfig.getConfig("database");

    this.configuration = getDefaultConfiguration();
    initConnectionParams();

    this.properties = config.getConfig("properties").entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, o -> o.getValue().unwrapped()));

    this.dataSource = getDefaultDataSource();
    this.configuration.set(this.dataSource);
  }

  private void initConnectionParams() {
    this.username = config.getString("username");
    this.password = config.getString("password");

    // When using Cloud SQL Factory the host will be just "google" with no port
    String address = config.getString("host");
    if (config.hasPath("port") && !config.getString("port").isEmpty()) {
      address += String.format(":%s", config.getInt("port"));
    }
    this.url = String.format("jdbc:postgresql://%s/%s", address, config.getString("name"));
  }

  private static Settings getDefaultSettings() {
    // https://www.jooq.org/doc/3.11/manual-single-page/#custom-settings
    return new Settings()
        .withExecuteLogging(true)
        .withFetchWarnings(true)
        .withAttachRecords(true)
        .withUpdatablePrimaryKeys(false)
        .withReturnAllOnUpdatableRecord(true)
        .withReflectionCaching(true)
        .withMapJPAAnnotations(false)
        .withQueryTimeout(5)
        .withMaxRows(0)
        .withFetchSize(0)
        .withRenderNameStyle(RenderNameStyle.QUOTED)
        .withRenderKeywordStyle(RenderKeywordStyle.UPPER)
        .withParamType(ParamType.NAMED)
        .withBackslashEscaping(BackslashEscaping.ON);
  }

  private static Configuration getDefaultConfiguration() {
    return new DefaultConfiguration()
        .set(getDefaultSettings())
        .set(SQLDialect.POSTGRES);
  }

  /**
   * Get configuration.
   *
   * @return database configuration
   */
  public Configuration getConfiguration() {
    return this.configuration;
  }

  /**
   * Configures and returns a HikariDataSource.
   *
   * @return DataSource
   *
   * @see <a href="https://jdbc.postgresql.org/documentation/head/connect.html#connection-parameters">PostgreSQL
   *     Connection Parameters</a>
   */
  private DataSource getDefaultDataSource() {
    HikariConfig hikariConfig = new HikariConfig();

    hikariConfig.setJdbcUrl(this.url);
    hikariConfig.setPoolName("DatabasePool");
    hikariConfig.setMaximumPoolSize(10);
    hikariConfig.setMinimumIdle(10);
    hikariConfig.setAutoCommit(false);

    if (this.username != null) {
      hikariConfig.setUsername(username);
    }
    if (this.password != null) {
      hikariConfig.setPassword(password);
    }

    if (this.properties != null) {
      this.properties.forEach((key, value) -> {
        LOG.trace(String.format("Adding Connection Property {%s: %s}", key, value));
        hikariConfig.addDataSourceProperty(key, value);
      });
    }

    LOG.info(String.format("Initializing Database Connection Pool - %s", this.url));

    return new HikariDataSource(hikariConfig);
  }

  /**
   * Get database config.
   *
   * @return config
   */
  public Config getConfig() {
    return config;
  }

  /**
   * Returns the HikariDataSource.
   *
   * @return DataSource
   */
  public DataSource getDataSource() {
    return dataSource;
  }

  /**
   * Jooq DSL.
   *
   * @return dsl context
   */
  public DSLContext dsl() {
    return Objects.nonNull(dslContext) ? dslContext : this.configuration.dsl();
  }

  public void setDslContext(@Nullable final DSLContext dsl) {
    this.dslContext = dsl;
  }

  /**
   * Close the database connection pool.
   */
  public void close() {
    dsl().close();
  }
}
