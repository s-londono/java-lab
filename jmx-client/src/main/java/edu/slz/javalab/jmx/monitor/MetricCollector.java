package edu.slz.javalab.jmx.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sun.font.EAttribute;

import javax.annotation.PostConstruct;
import javax.management.*;
import javax.management.remote.JMXConnector;
import java.io.IOException;
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

      // Read attributes of interest from each IMap MBean
      for (String mBeanName : allMBeanNames) {
        ObjectName objIMapMBeanName = new ObjectName(mBeanName);
        ObjectInstance iMapMBean = mbsc.getObjectInstance(objIMapMBeanName);
        logger.debug("Reading metrics from IMap MBean: {}...", mBeanName);

        List<Object> readIMapMetrics = readIMapMetrics(mbsc, mBeanName);
        logger.debug("IMap MBean {}. Metrics: {}...", mBeanName, readIMapMetrics.size());
      }
    } catch (IOException | MalformedObjectNameException | InstanceNotFoundException e) {
      logger.error("IOError on JMX connection", e);
    }
  }

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

  private class MBeanMetric {
    public String header;
    public String attributeName;

    public MBeanMetric(String header, String attributeName) {
      this.header = header;
      this.attributeName = attributeName;
    }
  }
}
