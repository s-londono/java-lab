package edu.slz.javalab.jmx.monitor.metric;

import edu.slz.javalab.jmx.monitor.JmxConnManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.management.*;
import javax.management.remote.JMXConnector;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MetricCollector {
  private final static Logger logger = LoggerFactory.getLogger(MetricCollector.class);

  private final JmxConnManager jmxConnMngr;

  private final List<MBeanMetric> mBeanMetrics;

  @Autowired
  public MetricCollector(JmxConnManager jmxConnMngr) {
    this.jmxConnMngr = jmxConnMngr;
    this.mBeanMetrics = Arrays.asList(
        new MBeanMetric("MapName", "name"),
        new MBeanMetric("LocalHeapCost", "localHeapCost"),
        new MBeanMetric("LocalOwnedEntryMemoryCost", "localOwnedEntryMemoryCost"),
        new MBeanMetric("LocalBackupEntryMemoryCost", "localBackupEntryMemoryCost"),
        new MBeanMetric("Size", "size"),
        new MBeanMetric("LocalBackupEntryCount", "localBackupEntryCount"),
        new MBeanMetric("LocalDirtyEntryCount", "localDirtyEntryCount"),
        new MBeanMetric("LocalBackupCount", "localBackupCount"),
        new MBeanMetric("LocalOwnedEntryCount", "localOwnedEntryCount")
    );
  }

  @PostConstruct
  public void initialize() {
    logger.info("Starting up monitor");
  }

  @Scheduled(fixedRate = 30000L)
  public void collect() {
    try (JMXConnector jmxConn = jmxConnMngr.connectToRemote("127.0.0.1", 1099)) {
      // Client is now connected to the MBean Server created by the JMX Agent, and can register MBeans and do operations
      MBeanServerConnection mbsc = jmxConn.getMBeanServerConnection();
      logger.info("Connected to MBeanServer. ConnId: {}", jmxConn.getConnectionId());

      // Search all Hazelcast Map MBeans on the Server
      ObjectName hzMapBeanNamePattern = new ObjectName("com.hazelcast:instance=*,type=IMap,*");
      Set<ObjectName> allMBeanObjectNames = mbsc.queryNames(hzMapBeanNamePattern, null);

      List<String> allMBeanNames = allMBeanObjectNames.stream()
          .map(ObjectName::getCanonicalName).sorted().collect(Collectors.toList());
      logger.info("Hazelcast Map MBeans found: {}", allMBeanNames.size());

      try (BufferedWriter resWriter = openResultsWriter()) {
        allMBeanObjectNames.stream()
          .sorted(ObjectName::compareTo)
          .forEach(objectName -> {
            MBeanMetricReader mReader = new HzIMapMetricReader();
            List<AttributeMetric<?>> metricsRead = mReader.read(mbsc, objectName);
            logger.info("IMap MBean {}. Metrics: {}...", objectName, metricsRead.size());
          });
      }
    } catch (IOException | MalformedObjectNameException e) {
      logger.error("IOError on JMX connection", e);
    }
  }

  /**
   * Reads the values of all attributes of interest about Hazelcast Maps and returns them in a list
   * @param mbsc MBeanServerConnection to connect to the MBean
   * @param mBeanName Name of the MBean containin the attributes to be read
   * @return List of values of attribute of interest in the IMap MBean
   */
  private List<Object> readIMapMetrics(MBeanServerConnection mbsc, String mBeanName) {
    List<Object> metricValues = new ArrayList<>(mBeanMetrics.size());

    for (MBeanMetric metric : mBeanMetrics) {
      try {
        ObjectName objIMapMBeanName = new ObjectName(mBeanName);
        metricValues.add(mbsc.getAttribute(objIMapMBeanName, metric.attributeName));
      } catch (IOException | OperationsException | MBeanException | ReflectionException e) {
        throw new RuntimeException("Error reading attribute " + metric.attributeName + " of " + mBeanName, e);
      }
    }

    return metricValues;
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
   * An MBean attribute to be read and which provides the value of a metric
   */
  private class MBeanMetric {
    public String header;
    public String attributeName;

    public MBeanMetric(String header, String attributeName) {
      this.header = header;
      this.attributeName = attributeName;
    }
  }
}
