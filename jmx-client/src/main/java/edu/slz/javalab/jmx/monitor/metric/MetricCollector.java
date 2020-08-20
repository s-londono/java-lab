package edu.slz.javalab.jmx.monitor.metric;

import edu.slz.javalab.jmx.monitor.AppConfig;
import edu.slz.javalab.jmx.monitor.JmxConnManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Component
public class MetricCollector {
  private final static Logger logger = LoggerFactory.getLogger(MetricCollector.class);

  private static final String COLUMN_SEPARATOR = ",";

  private static final int DEFAULT_PORT = 1591;

  private final JmxConnManager jmxConnMngr;

  private final List<String> targets;

  @Autowired
  public MetricCollector(AppConfig appConfig, JmxConnManager jmxConnMngr) {
    this.jmxConnMngr = jmxConnMngr;
    this.targets = appConfig.getTargets();
  }

  @PostConstruct
  public void initialize() {
    logger.info("Starting up monitor");
  }

  @Scheduled(fixedDelay = 30000L)
  public void collect() {
    targets.forEach(target -> {
      logger.info("Collecting metrics on: {}...", target);

      String[] targetComponents = target.split(":");
      String targetHost = targetComponents[0];
      int targetPort = (targetComponents.length > 1) ? Integer.parseInt(targetComponents[1]) : DEFAULT_PORT;

      try (JMXConnector jmxConn = jmxConnMngr.connectToRemote(targetHost, targetPort)) {
        // Client is now connected to the MBean Server created by the JMX Agent, and can register MBeans and do operations
        MBeanServerConnection mbsc = jmxConn.getMBeanServerConnection();

        logger.info("Connected to MBeanServer. ConnId: {}", jmxConn.getConnectionId());

        MBeanMetricReader mReader = new HzIMapMetricReader();

        executeMetricReader(targetHost, mbsc, mReader);
      } catch (IOException e) {
        logger.error("IOError on JMX connection", e);
      }
    });
  }

  /**
   * Runs a metrics reader on the specified MBeanServerConnection and writes the results to the appropriate
   * @param node Identifier of the node to take measurements on
   * @param mbsc Read the metrics from this MBeanServerConnection
   * @param mReader Metrics reader to be executed
   */
  private void executeMetricReader(String node, MBeanServerConnection mbsc, MBeanMetricReader mReader) {
    logger.info("Reading Metrics: {}. At: {}...", mReader, node);

    List<MBeanRecord> metricsRead = mReader.read(mbsc);

    if (metricsRead == null || metricsRead.size() == 0) {
      logger.warn("IMap MBean. Ignored empty Metrics: {}", metricsRead);
      return;
    }

    String strDate = ZonedDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    String resultsFileName = mReader.getName() + "-" + strDate + ".csv";
    Path resDir = Paths.get("logs");

    try {
      Files.createDirectories(resDir);
    } catch (IOException e) {
      logger.error("Error creating results directory " + resDir, e);
    }

    Path resultsFilePath = resDir.resolve(resultsFileName);
    boolean isNewResultsFile = !Files.exists(resultsFilePath);

    try (BufferedWriter resultsWriter = Files.newBufferedWriter(resultsFilePath,
      StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

      if (isNewResultsFile) {
        writeResultsHeaderRow(resultsWriter, metricsRead.get(0));
      }

      metricsRead.forEach(mBeanRecord -> writeResultsRow(resultsWriter, mBeanRecord, node));
    } catch (IOException e) {
      logger.error("Error writing results", e);
    }

    logger.info("IMap MBean. Metrics read: {}. ResultsFile: {}", metricsRead.size(), resultsFilePath);
  }

  /**
   * Writes the headers row into the specified results file writer
   * @param writer The headers row will be written into this file writer
   * @param mBeanRecord A metrics record measured on a specific MBean
   */
  private void writeResultsHeaderRow(BufferedWriter writer, MBeanRecord mBeanRecord) {
    Objects.requireNonNull(mBeanRecord);
    Objects.requireNonNull(mBeanRecord.getAttributeMetrics());

    try {
      writer.write("Time" + COLUMN_SEPARATOR);
      writer.write("Node" + COLUMN_SEPARATOR);

      for (AttributeMetric<?> metric : mBeanRecord.getAttributeMetrics()) {
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
   * @param mBeanRecord A metrics record to be written, measured on a specific MBean
   * @param node ID of the node the metrics were measured at
   */
  private void writeResultsRow(BufferedWriter writer, MBeanRecord mBeanRecord, String node) {
    Objects.requireNonNull(mBeanRecord);
    Objects.requireNonNull(mBeanRecord.getAttributeMetrics());

    try {
      writer.write(mBeanRecord.getTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) + COLUMN_SEPARATOR);
      writer.write(node + COLUMN_SEPARATOR);

      for (AttributeMetric<?> metric : mBeanRecord.getAttributeMetrics()) {
        String valMark = String.class.equals(metric.getAttributeClass()) ? "\"" : "";
        writer.write(valMark + metric.getValue() + valMark + COLUMN_SEPARATOR);
      }

      writer.newLine();
    } catch (IOException e) {
      throw new RuntimeException("Error writing results file header", e);
    }
  }

}
