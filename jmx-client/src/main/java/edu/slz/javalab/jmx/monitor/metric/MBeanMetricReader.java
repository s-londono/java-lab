package edu.slz.javalab.jmx.monitor.metric;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.List;

public interface MBeanMetricReader {

  List<AttributeMetric<?>> read(MBeanServerConnection mbsc, ObjectName mBeanName);

}
