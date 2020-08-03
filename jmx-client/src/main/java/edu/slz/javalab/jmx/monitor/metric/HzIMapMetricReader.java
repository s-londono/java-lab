package edu.slz.javalab.jmx.monitor.metric;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines the set of metrics that are to be read from MBeans corresponding to Hazelcast Maps
 */
public class HzIMapMetricReader implements MBeanMetricReader {
  private static final Logger logger = LoggerFactory.getLogger(HzIMapMetricReader.class);

  /**
   * Reads metrics from the MBean having the specified name
   * @param mbsc MBeanServerConnection to the JMX process from which the metrics are to be read
   * @param mBeanName Name of the MBean from which the metrics are to be extracted
   * @return List of metrics read, with their values obtained from the corresponding MBean attributes
   */
  public List<AttributeMetric<?>> read(MBeanServerConnection mbsc, ObjectName mBeanName) {
    List<AttributeMetric<?>> attributeMetrics = buildMetricsToRead();

      attributeMetrics.forEach(attribMetric -> {
        String attribName = attribMetric.getAttributeName();

        try {
          attribMetric.castAndSetValue(mbsc.getAttribute(mBeanName, attribName));
        } catch (Exception e) {
          logger.error("Error reading AttributeMetric {} from {}", attribName, mBeanName, e);
        }
      });

    return attributeMetrics;
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

}
