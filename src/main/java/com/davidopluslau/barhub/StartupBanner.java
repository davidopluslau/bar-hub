package com.davidopluslau.barhub;

import com.davidopluslau.barhub.config.BarHubConfigProvider;
import com.networknt.server.StartupHookProvider;
import com.networknt.service.SingletonServiceFactory;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupBanner implements StartupHookProvider {
  private static final Logger LOG = LoggerFactory.getLogger(StartupBanner.class);

  @Override
  public void onStartup() {
    Config config = SingletonServiceFactory.getBean(BarHubConfigProvider.class).getConfig();
    String version = config.getString("build.versionImpl");
    String env = config.getString("environment");

    // http://patorjk.com/software/taag/
    System.out.println();
    System.out.println("~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~");
    System.out.println(":                                                                       :");
    System.out.println(":                                                                       :");
    System.out.println(":                _   _       _                                          :");
    System.out.println(":               | | | |_   _| |__                                       :");
    System.out.println(":               | |_| | | | | '_ \\                                      :");
    System.out.println(":               |  _  | |_| | |_) |                                     :");
    System.out.println(":               |_| |_|\\__,_|_.__/                                      :");
    System.out.println(":                                                                       :");
    System.out.println(":                                                                       :");
    System.out.println(String.format(":                   v : %-48s:", version));
    System.out.println(String.format(":                 env : %-48s:", env));
    System.out.println(":                                                                       :");
    System.out.println("~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~%~");
    System.out.println();
    System.out.println();
  }

}
