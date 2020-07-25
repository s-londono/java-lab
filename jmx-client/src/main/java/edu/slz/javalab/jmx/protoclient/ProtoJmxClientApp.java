package edu.slz.javalab.jmx.protoclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.management.*;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
public class ProtoJmxClientApp {
  private static final Logger logger = LoggerFactory.getLogger(ProtoJmxClientApp.class);

  public static void main(String[] args) {
    ConfigurableApplicationContext appCtx = SpringApplication.run(ProtoJmxClientApp.class);
    logger.debug("Started Spring AppCtx: {}", appCtx);

//    protoManualJmxClient();
//    protoInvokeMemoryOperationDirectly();
//    protoInvokeMemoryOperationUsingProxy();

    protoMonitorMemory();

    System.exit(0);
  }

  private static void protoMonitorMemory() {
    try (JMXConnector jmxConn = ProtoJmxClientApp.getJmxConnector()) {
      // Client is now connected to the MBean Server created by the JMX Agent, and can register MBeans and do operations
      MBeanServerConnection mbsc = jmxConn.getMBeanServerConnection();
      logger.info("Connected to MBeanServer");

      // Get instance of the Memory MBean. Class: sun.management.MemoryImpl
      ObjectName memoryBeanName = new ObjectName("java.lang:type=Memory");
      ObjectInstance mBeanMemory = mbsc.getObjectInstance(memoryBeanName);
      logger.info("Memory MBean: {}", mBeanMemory);

      int i = 0;
      String sampleTimestamp;
      String memInit;
      String memMax;
      String memCommitted;
      String memUsed;

      try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("logs", "output.csv"),
          StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
        while (i < 100) {
          i += 1;

          // Read attribute from an MBean
          CompositeDataSupport attrMemUsage =
              (CompositeDataSupport) mbsc.getAttribute(memoryBeanName, "HeapMemoryUsage");

          sampleTimestamp = String.valueOf(Instant.now().toEpochMilli());
          memInit = String.valueOf(attrMemUsage.get("init"));
          memMax = String.valueOf(attrMemUsage.get("max"));
          memCommitted = String.valueOf(attrMemUsage.get("committed"));
          memUsed = String.valueOf(attrMemUsage.get("used"));

          logger.info("Timestamp:       {}", sampleTimestamp);
          logger.info("Memory initial:  {}", memInit);
          logger.info("Memory maximum:  {}", memMax);
          logger.info("Memory commited: {}", memCommitted);
          logger.info("Memory used:     {}", memUsed);

          writer.write(String.join(",", new String[] {
              sampleTimestamp,
              memInit,
              memMax,
              memCommitted,
              memUsed}));

          writer.newLine();
          writer.flush();

          Thread.sleep(5000L);
        }
      }
    } catch (IOException | OperationsException | ReflectionException | MBeanException | InterruptedException e) {
      logger.error("Error connecting to MBeanServer", e);
    } catch (Exception ex) {
      logger.error("Unknown error", ex);
    }
  }

  private static void protoManualJmxClient() {
    try (JMXConnector jmxConn = ProtoJmxClientApp.getJmxConnector()) {
      // Client is now connected to the MBean Server created by the JMX Agent, and can register MBeans and do operations
      MBeanServerConnection mbsc = jmxConn.getMBeanServerConnection();
      logger.info("Connected to MBeanServer. Default domain: {}", mbsc.getDefaultDomain());

      List<String> lstDomains = Arrays.stream(mbsc.getDomains()).sorted().collect(Collectors.toList());
      logger.info("\nMBean Domains: \n- {}", String.join("\n-", lstDomains));

      // Get all MBeans from the Server
      Set<ObjectName> allMBeanObjectNames = mbsc.queryNames(null, null);
      List<String> allMBeanNames = allMBeanObjectNames.stream()
          .map(ObjectName::getCanonicalName)
          .sorted()
          .collect(Collectors.toList());
      logger.info("\nAll MBeans: \n- {}", String.join("\n-", allMBeanNames));

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

      // Get instance of the Memory MBean. Class: sun.management.MemoryImpl
      ObjectInstance mBeanMemory = mbsc.getObjectInstance(new ObjectName("java.lang:type=Memory"));
      logger.info("\nMBean: {}", mBeanMemory);
    } catch (IOException | OperationsException | ReflectionException e) {
      logger.error("Error connecting to MBeanServer", e);
    }
  }

  private static void protoInvokeMemoryOperationDirectly() {
    try (JMXConnector jmxConn = ProtoJmxClientApp.getJmxConnector()) {
      // Client is now connected to the MBean Server created by the JMX Agent, and can register MBeans and do operations
      MBeanServerConnection mbsc = jmxConn.getMBeanServerConnection();
      logger.info("Connected to MBeanServer");

      // Get instance of the Memory MBean. Class: sun.management.MemoryImpl
      ObjectName memoryBeanName = new ObjectName("java.lang:type=Memory");
      ObjectInstance mBeanMemory = mbsc.getObjectInstance(memoryBeanName);
      logger.info("Memory MBean: {}", mBeanMemory);

      // Invoke operation on an Memory MBean
      mbsc.invoke(memoryBeanName, "gc", new Object[] {}, new String[] {});
    } catch (IOException | OperationsException | ReflectionException | MBeanException e) {
      logger.error("Error connecting to MBeanServer", e);
    }
  }

  private static void protoInvokeMemoryOperationUsingProxy() {
    try (JMXConnector jmxConn = ProtoJmxClientApp.getJmxConnector()) {
      // Client is now connected to the MBean Server created by the JMX Agent, and can register MBeans and do operations
      MBeanServerConnection mbsc = jmxConn.getMBeanServerConnection();
      logger.info("Connected to MBeanServer");

      // Get instance of the Memory MBean. Class: sun.management.MemoryImpl
      ObjectName memoryBeanName = new ObjectName("java.lang:type=Memory");
      ObjectInstance mBeanMemory = mbsc.getObjectInstance(memoryBeanName);
      logger.info("Memory MBean: {}", mBeanMemory);

      // Invoke operation on an Memory MBean using proxy
      IMemoryImpl fooProxy = JMX.newMBeanProxy(mbsc, memoryBeanName, IMemoryImpl.class);
      fooProxy.gc();
    } catch (IOException | OperationsException e) {
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

  private static JMXConnector getJmxConnector() throws IOException {
    JMXServiceURL jmxSrvUrl = ProtoJmxClientApp.buildJmxServiceUrl();

    Map<String, Object> jmxEnv = new HashMap<>();
    jmxEnv.put("com.sun.management.jmxremote.authenticate", false);
    jmxEnv.put("com.sun.management.jmxremote.ssl", false);
    jmxEnv.put("com.sun.management.jmxremote.ssl.need.client.auth", false);
    jmxEnv.put("com.sun.management.jmxremote.registry.ssl", false);

    JMXConnector jmxConn = JMXConnectorFactory.connect(jmxSrvUrl, jmxEnv);
    logger.info("JMXClient connected. {}", jmxConn);

    return jmxConn;
  }

  /**
   * Inner interfaces can be used to create proxies of MBeans. For instance, this interface serves as proxy for
   * the gc operation of MBean java.lang:type=Memory
   */
  public interface IMemoryImpl {
    void gc();
  }

}
