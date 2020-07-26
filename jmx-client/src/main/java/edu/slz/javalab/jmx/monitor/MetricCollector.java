package edu.slz.javalab.jmx.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.management.remote.JMXConnector;
import java.io.IOException;

@Component
public class MetricCollector {
  private final static Logger logger = LoggerFactory.getLogger(MetricCollector.class);

  private final JmxConnManager jmxConnMngr;

  @Autowired
  public MetricCollector(JmxConnManager jmxConnMngr) {
    this.jmxConnMngr = jmxConnMngr;
  }

  @PostConstruct
  public void initialize() {
    logger.info("Starting up monitor");
  }

  @Scheduled(fixedRate = 30000L)
  public void collect() {
    try (JMXConnector devJmxConn = jmxConnMngr.connectToRemote("127.0.0.1", 1099)) {
      logger.info("Collecting metrics. JMX {}", devJmxConn);
    } catch (IOException e) {
      logger.error("IOError on JMX connection", e);
    }
  }
}
