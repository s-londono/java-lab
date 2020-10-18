# Spring Boot Configuration Properties

## Property source

- In plain Spring, we have to specify property sources using for example, @PropertySource or specifying a path to a properties file.

- Spring Boot automatically detects application.properties (or application.yml) files stored at src/main/resources.

- We can also provide property values through: 
  - System properties. 
  - Command line arguments. 
  - Environment variables.

  Note that system properties override property values.

```bash
java -jar app.jar --property="value"

java -Dproperty.name="value" -jar app.jar

export name=value
java -jar app.jar
```

## Inject property values

- Use the @Value annotation with Spring resolve placeholders (`${}`) to use property values within Beans. We can set a default value after the : symbol.

```java
@Value( "${jdbc.url:aDefaultUrl}" )
private String jdbcUrl;
```

- Note that @Value also supports more advanced expressions using SpEL expressions (`#{}`).

```java
@Value("#{systemProperties['unknown'] ?: 'some default'}")
private String spelSomeDefault;

@Value("#{'${listOfValues}'.split(',')}")
private List<String> valuesList;
```


## Resources

- [https://www.baeldung.com/properties-with-spring](https://www.baeldung.com/properties-with-spring)
- [https://www.baeldung.com/spring-value-annotation](https://www.baeldung.com/spring-value-annotation)