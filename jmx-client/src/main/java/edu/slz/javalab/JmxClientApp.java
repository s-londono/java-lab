package edu.slz.javalab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class JmxClientApp {
  private static final Logger logger = LoggerFactory.getLogger(JmxClientApp.class);

  public static void main(String[] args) {
    ConfigurableApplicationContext appCtx = SpringApplication.run(JmxClientApp.class);
    logger.debug("Started Spring AppCtx: {}", appCtx);

    protoManualJmxClient();
  }

  private static void protoJ256JmxClient() {

  }

  private static void protoManualJmxClient() {
    Map<String, Object> jmxParams = new HashMap<>();
    jmxParams.put("com.sun.management.jmxremote.authenticate", false);
    jmxParams.put("com.sun.management.jmxremote.ssl", false);
    jmxParams.put("com.sun.management.jmxremote.ssl.need.client.auth", false);
    jmxParams.put("com.sun.management.jmxremote.registry.ssl", false);

    JMXServiceURL jmxUrl = JmxClientApp.buildJmxServiceUrl();

    try (JMXConnector jmxConn = JMXConnectorFactory.connect(jmxUrl, jmxParams)) {
      logger.info("JMXClient connected. {}", jmxConn);

      // Client is now connected to the MBean Server created by the JMX Agent, and can register MBeans and do operations
      MBeanServerConnection mbsc = jmxConn.getMBeanServerConnection();
      logger.info("Connected to MBeanServer. Default domain: {}", mbsc.getDefaultDomain());

      List<String> lstDomains = Arrays.stream(mbsc.getDomains()).sorted().collect(Collectors.toList());
      logger.info("\nMBean Domains: {}", String.join("\n-", lstDomains));

      // Get all MBeans from the Server
      Set<ObjectName> allMBeanObjectNames = mbsc.queryNames(null, null);
      List<String> allMBeanNames = allMBeanObjectNames.stream()
          .map(ObjectName::getCanonicalName)
          .sorted()
          .collect(Collectors.toList());
      logger.info("\nAll MBeans: {}", String.join("\n-", allMBeanNames));

      // Get the attributes and operations of an MBean:
      MBeanInfo mBeanInfo = mbsc.getMBeanInfo(new ObjectName("java.lang:type=Memory"));
      String mBeanClass = mBeanInfo.getClassName();
      String mBeanDesc = mBeanInfo.getDescription();
      String mBeanAttribs = Arrays.stream(mBeanInfo.getAttributes())
          .map(MBeanAttributeInfo::getName)
          .collect(Collectors.joining(", "));
      String mBeanOps = Arrays.stream(mBeanInfo.getOperations())
          .map(MBeanOperationInfo::getName)
          .collect(Collectors.joining(", "));

      String strMBeanInfo = String.format("- Class: %s\n- Desc: %s\n- Attributes: %s\n- Operations: %s",
          mBeanClass, mBeanDesc, mBeanAttribs, mBeanOps);
      logger.info("\nBean Details:\n{}", strMBeanInfo);
    } catch (IOException | OperationsException | ReflectionException e) {
      logger.error("Error connecting to MBeanServer", e);
    }
  }

  private static JMXServiceURL buildJmxServiceUrl() {
    String hostName = "localhost";
    int portNum = 1099;

    String strJmxUrl = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", hostName, portNum);

    try {
      return new JMXServiceURL(strJmxUrl);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Invalid JMX URL");
    }
  }

}
