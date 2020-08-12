package edu.slz.javalab.jmx.monitor.metric;

import edu.slz.javalab.jmx.monitor.JmxConnManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MetricCollector {
  private final static Logger logger = LoggerFactory.getLogger(MetricCollector.class);

  private static final String COLUMN_SEPARATOR = ",";

  private static final int DEFAULT_PORT = 1591;

  private final JmxConnManager jmxConnMngr;

  private final List<String> targets;


  @Autowired
  public MetricCollector(JmxConnManager jmxConnMngr) {
    this.jmxConnMngr = jmxConnMngr;

    this.targets = Arrays.asList(
      "localhost"
    );

    this.targetPort = 1099;
  }

  @PostConstruct
  public void initialize() {
    logger.info("Starting up monitor");
  }

  @Scheduled(fixedRate = 30000L)
  public void collect() {
    String target = "127.0.0.1:1099";
    String[] targetComponents = target.split(":");

    String targetHost = targetComponents[0];
    int targetPort = (targetComponents.length > 1) ? Integer.parseInt(targetComponents[1]) : DEFAULT_PORT;

    try (JMXConnector jmxConn = jmxConnMngr.connectToRemote(targetHost, targetPort)) {
      // Client is now connected to the MBean Server created by the JMX Agent, and can register MBeans and do operations
      MBeanServerConnection mbsc = jmxConn.getMBeanServerConnection();

      logger.info("Connected to MBeanServer. ConnId: {}", jmxConn.getConnectionId());

      // Search all Hazelcast Map MBeans on the Server
      ObjectName hzMapBeanNamePattern = new ObjectName("com.hazelcast:instance=*,type=IMap,*");
      Set<ObjectName> allMBeanObjectNames = mbsc.queryNames(hzMapBeanNamePattern, null);

      List<ObjectName> sortedMBeanObjectNames =
        Optional.of(allMBeanObjectNames).orElse(Collections.emptySet()).stream()
          .sorted(Comparator.comparing(ObjectName::getCanonicalName))
          .collect(Collectors.toList());

      try (BufferedWriter resWriter = openResultsWriter()) {
        boolean headerWritten = false;

        for (ObjectName mBeanObjName : sortedMBeanObjectNames) {
          logger.debug("IMap MBean {}. Reading Metrics...", mBeanObjName);

          MBeanMetricReader mReader = new HzIMapMetricReader();
          List<AttributeMetric<?>> metricsRead = mReader.read(mbsc, mBeanObjName);

          if (metricsRead == null || metricsRead.size() == 0) {
            logger.warn("IMap MBean {}. Ignored empty Metrics: {}", mBeanObjName, metricsRead);
            continue;
          }

          if (!headerWritten) {
            writeResultsHeaderRow(resWriter, metricsRead);
            headerWritten = true;
          }

          writeResultsRow(resWriter, metricsRead, "Node", "");

          logger.info("IMap MBean {}. Metrics read: {}", mBeanObjName, metricsRead.size());
        }
      }
    } catch (IOException | MalformedObjectNameException e) {
      logger.error("IOError on JMX connection", e);
    }
  }

  /**
   * Opens a properly configured writer to the results CSV file
   * @return BufferedWriter for the results file
   */
  private BufferedWriter openResultsWriter() throws IOException {
    Path resDir = Paths.get("logs");
    Files.createDirectories(resDir);

    return Files.newBufferedWriter(resDir.resolve("hz-map-metrics.csv"), StandardCharsets.UTF_8,
      StandardOpenOption.CREATE, StandardOpenOption.APPEND);
  }

  /**
   * Writes the headers row into the specified results file writer
   * @param writer The headers row will be written into this file writer
   * @param metrics List of metrics corresponding to a results record
   */
  private void writeResultsHeaderRow(BufferedWriter writer, List<AttributeMetric<?>> metrics) {
    Objects.requireNonNull(metrics);

    try {
      writer.write("Time" + COLUMN_SEPARATOR);
      writer.write("Node" + COLUMN_SEPARATOR);
      writer.write("Type" + COLUMN_SEPARATOR);

      for (AttributeMetric<?> metric : metrics) {
        writer.write(metric.getName() + COLUMN_SEPARATOR);
      }

      writer.newLine();
    } catch (IOException e) {
      throw new RuntimeException("Error writing results file header", e);
    }
  }

  /**
   * Writes a row of metric records into the results file
   * @param writer The headers row will be written into this file writer
   * @param metrics List of metrics corresponding to the results record to be written
   * @param node ID of the node the metrics were measured at
   * @param type Type of metrics recorded (usually part of the name of the MBean)
   */
  private void writeResultsRow(BufferedWriter writer, List<AttributeMetric<?>> metrics, String node, String type) {
    Objects.requireNonNull(metrics);

    Long timestamp = Instant.now().toEpochMilli();

    try {
      writer.write(timestamp + COLUMN_SEPARATOR);
      writer.write(node + COLUMN_SEPARATOR);
      writer.write(type + COLUMN_SEPARATOR);

      for (AttributeMetric<?> metric : metrics) {
        String valMark = String.class.equals(metric.getAttributeClass()) ? "\"" : "";
        writer.write(valMark + metric.getValue() + valMark + COLUMN_SEPARATOR);
      }

      writer.newLine();
    } catch (IOException e) {
      throw new RuntimeException("Error writing results file header", e);
    }
  }

}
