package edu.slz.javalab.jmx.monitor.metric;

import java.util.List;

public class MBeanRecord {

  private String objectName;

  private long timestamp;

  private List<AttributeMetric<?>> attributeMetrics;

  public MBeanRecord(String objectName, long timestamp, List<AttributeMetric<?>> attributeMetrics) {
    this.timestamp = timestamp;
    this.objectName = objectName;
    this.attributeMetrics = attributeMetrics;
  }

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public List<AttributeMetric<?>> getAttributeMetrics() {
    return attributeMetrics;
  }

  public void setAttributeMetrics(List<AttributeMetric<?>> attributeMetrics) {
    this.attributeMetrics = attributeMetrics;
  }

  @Override
  public String toString() {
    return "MBeanRecord{" +
      "objectName='" + objectName + '\'' +
      ", timestamp=" + timestamp +
      ", attributeMetrics=" + (attributeMetrics != null ? attributeMetrics.size() : null) +
      '}';
  }
}
