package edu.slz.javalab.jmx.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class JmxMonitorApp {
  private static final Logger logger = LoggerFactory.getLogger(JmxMonitorApp.class);

  public static void main(String[] args) {
    // Set a handler to be invoked when the App abruptly terminates due to an uncaught exception
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
      logger.error("Terminating due to uncaught exception. Thread: {}", t, e);
      System.exit(1);
    });

    Thread mainThread = Thread.currentThread();

    // Registers a virtual-machine shutdown hook, just to log information about the termination sequence
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      logger.info("JmxMonitor shutting down...");

      try {
        mainThread.join(15000L);
      } catch (InterruptedException e) {
        logger.warn("MainThread taking too long to terminate");
      }
    }));

    ConfigurableApplicationContext appCtx = SpringApplication.run(JmxMonitorApp.class);
    logger.info("JmxMonitor starting up: {}...", appCtx.getApplicationName());

    System.exit(0);
  }
}
