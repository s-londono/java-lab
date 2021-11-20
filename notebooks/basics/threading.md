### Shutdown Hooks and Uncaught Exceptions

It might be useful to set a DefaultUncaughtExceptionHandler on the application, to ensure that any exception that slips  
uncaught at least gets properly logged. According to the Javadocs:

> The handler gets invoked when a thread abruptly terminates due to an uncaught exception, and no other handler 
> has been defined for that thread. By setting the default uncaught exception handler, an application can change 
> the way in which uncaught exceptions are handled (such as logging to a specific device, or file) for those 
> threads that would already accept whatever "default" behavior the system provided. 

Shutdown Hooks can also come handy. These are threads that get executed when the application starts to shut down.
Note how invoking join on the main thread delays the termination of the JVM for the specified number of milliseconds. 
This is because the main thread won't die until all Shutdown Hooks have finished.

```java
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
        // This causes termination of the JVM to be delayed by 15 seconds!
        mainThread.join(15000L);
      } catch (InterruptedException e) {
        logger.warn("MainThread taking too long to terminate");
      }
    }));

    ConfigurableApplicationContext appCtx = SpringApplication.run(JmxMonitorApp.class);
    logger.info("JmxMonitor starting up: {}...", appCtx.getApplicationName());

    System.exit(0);
}
```

