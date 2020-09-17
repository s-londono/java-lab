## Spring Boot Hazelcast Node

To enable a Hazelcast Instance on a Spring Boot application:

1. Add the following dependencies to the POM file.

  ```xml
  <dependencies>
    <dependency>
      <groupId>com.hazelcast</groupId>
      <artifactId>hazelcast</artifactId>
      <version>4.0.1</version>
    </dependency>
    <dependency>
      <groupId>com.hazelcast</groupId>
      <artifactId>hazelcast-spring</artifactId>
      <version>4.0.1</version>
    </dependency>
  </dependencies>
  ```
2. Define a Bean of class com.hazelcast.config.Config:

```java
@Configuration
public class ContextConfiguration {

  private final ConfigProperties configProps;

  @Autowired
  public ContextConfiguration(ConfigProperties configProps) {

    this.configProps = configProps;
  }

  @Bean
  public Config hazelcastConfig() {

    Config config = new Config();
    NetworkConfig netConfig = config.getNetworkConfig();

    // Can define the interfaces to connect through. Otherwise will connect through the first one found
    if (this.configProps.getInterfaces() != null) {
      // Specify the network interfaces Hazelcast should use. Very useful for compatibility with Docker Swarm
      netConfig.getInterfaces()
          .setEnabled(true)
          .setInterfaces(this.configProps.getInterfaces());
    }

    // A MembersList being specified, means that we want to use TCP Discovery
    if (this.configProps.getMembersList() != null) {
      // Disable MultiCast discovery, which is enabled by default
      netConfig.getJoin()
          .getMulticastConfig()
          .setEnabled(false);

      // Setup TCP discovery
      netConfig.getJoin()
          .getTcpIpConfig()
          .setEnabled(true)
          .setMembers(this.configProps.getMembersList());
    }

    return config;
  }
}
```


### References

- [Spring Boot features: Hazelcast](https://docs.spring.io/spring-boot/docs/1.5.3.RELEASE/reference/html/boot-features-hazelcast.html)
- [Enabling Hazelcast integration with Spring](https://docs.hazelcast.org/docs/latest/manual/html-single/#integration-with-spring)

