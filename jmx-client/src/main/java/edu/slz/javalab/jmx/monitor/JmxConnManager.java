package edu.slz.javalab.jmx.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilitarian bean to create and manage JMX connections
 */
@Component
public class JmxConnManager {

  private AppConfig props;

  @Autowired
  public JmxConnManager(AppConfig props) {
    this.props = props;
  }

  /**
   * Creates and returns an open JMXConnector with the specified host and port
   * @param host Host to connect to
   * @param port Remote JMX port to connect through
   * @return Open JMXConnector
   */
  public JMXConnector connectToRemote(String host, int port) {
    String jmxUrl = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", host, port);
    JMXServiceURL targetJmxUrl = null;

    try {
      targetJmxUrl = new JMXServiceURL(jmxUrl);
    } catch (MalformedURLException e) {
      throw new RuntimeException("Invalid JMX URL");
    }

    Map<String, Object> jmxEnv = new HashMap<>();
    jmxEnv.put("com.sun.management.jmxremote.authenticate", props.getJmxremoteAuthenticate());
    jmxEnv.put("com.sun.management.jmxremote.ssl", props.getJmxremoteSslEnabled());
    jmxEnv.put("com.sun.management.jmxremote.ssl.need.client.auth", props.getJmxremoteSslNeedClientAuth());
    jmxEnv.put("com.sun.management.jmxremote.registry.ssl", props.getJmxremoteRegistrySsl());

    try {
      return JMXConnectorFactory.connect(targetJmxUrl, jmxEnv);
    } catch (IOException e) {
      throw new RuntimeException("JMX connection failed " + jmxUrl);
    }
  }
}
