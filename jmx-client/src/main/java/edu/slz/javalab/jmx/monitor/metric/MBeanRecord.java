package edu.slz.javalab.jmx.monitor.metric;

import java.time.ZonedDateTime;
import java.util.List;

public class MBeanRecord {

  private String objectName;

  private ZonedDateTime time;

  private List<AttributeMetric<?>> attributeMetrics;

  public MBeanRecord(String objectName, ZonedDateTime time, List<AttributeMetric<?>> attributeMetrics) {
    this.time = time;
    this.objectName = objectName;
    this.attributeMetrics = attributeMetrics;
  }

  public String getObjectName() {
    return objectName;
  }

  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }

  public ZonedDateTime getTime() {
    return time;
  }

  public void setTime(ZonedDateTime time) {
    this.time = time;
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
      ", time=" + time +
      ", attributeMetrics=" + (attributeMetrics != null ? attributeMetrics.size() : null) +
      '}';
  }
}
