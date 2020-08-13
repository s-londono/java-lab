package edu.slz.javalab.jmx.monitor.metric;

import javax.management.MBeanServerConnection;
import java.util.List;

public interface MBeanMetricReader {

  /**
   * Reads metrics from one or more of similar MBean Objects. Each implementation defines the metrics and
   * the type of MBean Objects to be read
   * @param mbsc MBeanServerConnection to the JMX process from which the metrics are to be read
   * @return List of MBean metrics read. Records correspond to MBean Objects, which contain attribute metrics
   */
  List<MBeanRecord> read(MBeanServerConnection mbsc);

}
