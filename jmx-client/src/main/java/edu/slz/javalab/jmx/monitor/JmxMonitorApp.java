package edu.slz.javalab.jmx.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Scanner;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppConfig.class)
public class JmxMonitorApp {
  private static final Logger logger = LoggerFactory.getLogger(JmxMonitorApp.class);

  public static void main(String[] args) {
    // Set a handler to be invoked when the App abruptly terminates due to an uncaught exception
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
      logger.error("Terminating due to uncaught exception. Thread: {}", t, e);
      System.exit(1);
    });

    // Registers a virtual-machine shutdown hook, just to log information about the termination sequence
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      logger.info("JmxMonitor shutting down...");
    }, "MainShtdwnHk"));

    ConfigurableApplicationContext appCtx = SpringApplication.run(JmxMonitorApp.class);
    logger.info("JmxMonitor starting up: {}...", appCtx.getApplicationName());

    try (Scanner in = new Scanner(System.in)) {
      String option = null;

      while (!"stop".equalsIgnoreCase(option)) {
        System.out.println("\nPlease enter STOP to terminate the monitor: ");
        option = in.nextLine().trim();
      }
    }

    System.exit(0);
  }
}
