package edu.slz.javalab.jmx.monitor.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Defines the set of metrics that are to be read from MBeans corresponding to Hazelcast Maps
 */
public class HzIMapMetricReader implements MBeanMetricReader {
  private static final Logger logger = LoggerFactory.getLogger(HzIMapMetricReader.class);

  @Override
  public String getName() {
    return "HzIMap";
  }

  @Override
  public List<MBeanRecord> read(MBeanServerConnection mbsc) {
    List<MBeanRecord> resultsMatrix = new ArrayList<>();

    // Search all Hazelcast Map MBeans on the Server
    Set<ObjectName> allMBeanObjectNames;

    try {
      ObjectName hzMapBeanNamePattern = new ObjectName("com.hazelcast:instance=*,type=IMap,*");
      allMBeanObjectNames = mbsc.queryNames(hzMapBeanNamePattern, null);
    } catch (MalformedObjectNameException | IOException e) {
      throw new RuntimeException("Error reading IMap MBeanObjects", e);
    }

    List<ObjectName> sortedMBeanObjectNames =
      Optional.of(allMBeanObjectNames).orElse(Collections.emptySet()).stream()
        .sorted(Comparator.comparing(ObjectName::getCanonicalName))
        .collect(Collectors.toList());

    for (ObjectName mBeanObjName : sortedMBeanObjectNames) {
      List<AttributeMetric<?>> attributeMetrics = buildMetricsToRead();
      long samplingStartedAt = Instant.now().toEpochMilli();

      logger.debug("IMap MBean {}. Reading Metrics. At: {}...", mBeanObjName, samplingStartedAt);

      attributeMetrics.forEach(attribMetric -> {
        String attribName = attribMetric.getAttributeName();

        try {
          attribMetric.castAndSetValue(mbsc.getAttribute(mBeanObjName, attribName));
        } catch (Exception e) {
          logger.warn("Failed to read Attribute: {} of: {}", attribName, mBeanObjName);
        }
      });

      resultsMatrix.add(new MBeanRecord(mBeanObjName.getCanonicalName(), samplingStartedAt, attributeMetrics));
    }

    return resultsMatrix;
  }

  private List<AttributeMetric<?>> buildMetricsToRead() {
    List<AttributeMetric<?>> metrics = new ArrayList<>();

    Collections.addAll(metrics,
        new AttributeMetric<>("MapName", "name", String.class),
        new AttributeMetric<>("HeapCost", "localHeapCost", Long.class),
        new AttributeMetric<>("OwnedEntryMemoryCost", "localOwnedEntryMemoryCost", Long.class),
        new AttributeMetric<>("BackupEntryMemoryCost", "localBackupEntryMemoryCost", Long.class),
        new AttributeMetric<>("Size", "size", Integer.class),
        new AttributeMetric<>("BackupEntryCount", "localBackupEntryCount", Long.class),
        new AttributeMetric<>("DirtyEntryCount", "localDirtyEntryCount", Long.class),
        new AttributeMetric<>("BackupCount", "localBackupCount", Integer.class),
        new AttributeMetric<>("OwnedEntryCount", "localOwnedEntryCount", Long.class)
    );

    return metrics;
  }

  @Override
  public String toString() {
    return getName();
  }
}
