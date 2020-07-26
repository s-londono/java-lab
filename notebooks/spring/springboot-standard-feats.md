### Task Scheduling

Annotation [@Scheduled](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html). 
Allows running tasks periodically. Supports a way of defining task execution times in CRON format.

Make sure to add @EnableScheduling on the @SpringBootApplication, or on a @Configuration class.

Simple applications that do not keep running by default must be kept from terminating as long as required to run the 
scheduled tasks. For example, by setting the main thread to sleep for a while.

```java
@SpringBootApplication
@EnableScheduling
public class JmxMonitorApp {
  private static final Logger logger = LoggerFactory.getLogger(JmxMonitorApp.class);

  public static void main(String[] args) {
    ConfigurableApplicationContext appCtx = SpringApplication.run(JmxMonitorApp.class);
    logger.info("JmxMonitor starting up: {}...", appCtx.getApplicationName());

    try {
      Thread.sleep(130000L);
    } catch (InterruptedException e) {
      logger.error("Interrupted while sleeping", e);
    }
  }
}
```  

```java
@Component
public class MetricCollector {
  private final static Logger logger = LoggerFactory.getLogger(MetricCollector.class);

  @Scheduled(fixedRate = 30000L, initialDelay = 2000L)
  public void collect() {
    logger.info("Collecting metrics...");
  }
}
```