# Unit Testing with Spring Boot

* Include the dependency spring-boot-starter-test with test scope, so that it’s not put into releases:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-test</artifactId>
	<scope>test</scope>
</dependency>
```

## Simple Tests

* The simplest of tests requires no annotations whatsoever on the class. Only on the test methods:

```java
public class TestSerialization {
  private static Logger logger = LoggerFactory.getLogger(TestSerialization.class);
  
  ObjectMapper jsonMapper = new ObjectMapper();

  @Test
  public void testFallbackDeserialization() {  
    String strFllbk = "{\"object\":\"page\",\"entry\":[]}";
    
    GetCallback cllbk = null;
    
    try {
      cllbk = jsonMapper.readValue(strFllbk, GetCallback.class);
    } 
    catch (IOException e) {
      e.printStackTrace();
    }
    
    Assert.assertNotNull(cllbk);
  }

  ...
}
```

## Advanced Tests

* Tests should be set to run with SpringRunner.class, otherwise annotations such as @SpringBootTest will be ignored. 

* Spring Boot tests are annotated with @SpringBootTest, which replaces the @ContextConfiguration annotation used in the Standard Spring Test Framework. 

* By default, @SpringBootTest looks for a SpringAplication class in the project (i.e. a @SpringBootConfiguration) and uses it to load the ApplicationContext. This is the case when no @ContextConfiguration is specified and no @Configuration classes are nested in the test. 

   Note that @ActiveProfiles can be used to define the profile to run the test with. The properties attribute of @SpringBootTest can be used to override the values of specific properties.

```java
@RunWith(SpringRunner.class)
@SpringBootTest(properties={"cat.prop1=[\"v1\"]", "cat.prop2=abc"})
@ActiveProfiles({"local"})
public class SpringBootAutoLoadedTest {
    ...
}
```

## Customizing and extending test ApplicationContext

* To define use a custom Application Context configuration from scratch, add a nested @Configuration class to the test. Spring Boot Test will use this @Configuration class to load the ApplicationContext instead of the project's SpringApplication. That is, a nested @Configuration *overrides* the default Application Context configuration of Spring Boot Tests.

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootManuallyLoadedTest {
  ...

  @Configuration
  public static class ManualTestConfig {
    // Bean definitions...
  }

}
```

* To combine the default test ApplicationContext configuration with some customizations, use @TestConfiguration. In contrast with a nested @Configuration, a nested @TestConfiguration does not prevent the SpringApplication from being used to load the Application Context of the test, but is added up to its configuration. That is, a nested @Configuration *extends* the default Application Context configuration of Spring Boot Tests.

   According to the documentation, @TestConfiguration:
   > "Can be used to define additional beans or customizations for a test. Unlike regular @Configuration classes the use of @TestConfiguration does not prevent auto-detection of @SpringBootConfiguration".

```java
@RunWith(SpringRunner.class)
@SpringBootTest(properties={"cat.prop1=abc"})
@ActiveProfiles({"local"})
public class SpringBootAutoLoadedTest {
  ...

  @TestConfiguration
  public static class BotApiBeansConfig {
    
    @NotBlank
    @Value("${cat.prop1}")
    private String prop1;

    @Bean("someBeanName")
    SomeBean someBean() {
      return new SomeBean();
    }

  }

}
```

* It's also possible to specify an @ContextConfiguration on the test. In that case, the @SpringBootTest will not use the SpringApplication configuration to load the ApplicationContext.  This is equivalent to setting the classes attribute in @SpringBootTest to a class annotated with @Configuration (e.g. @SpringBootTest(classes={BotCoreService.class}))
<!-- TODO: Validate this claim. See example at bot-core.BotPingCommandTest -->

* The annotation @Import can be used in @TestConfiguration annotated classes to load additional  configurations in the ApplicationContext of the test.
<!-- TODO: Add example -->

* Use the @MockBean annotation to define mocks of beans to be used in the test. Is enabled by default in Spring Boot Tests (those that use annotations such as @SpringBootTest), otherwise can be enabled manually by adding the listener: @TestExecutionListeners(MockitoTestExecutionListener.class).
<!-- TODO: Add examples, putting @MockBean on attributes, @Configuration classes and Test Classes -->

```java
@TestConfiguration
@MockBean({KeyValueRepository.class, FbMessageBuilder.class})
public static class TestAppCtxConfig {
  @Bean
  public BeansProvider beansProvider() {
    return new TestBeansProvider();
  }
}
```

Which is equivalent to:

```java
@TestConfiguration
public static class TestAppCtxConfig {
  @Bean
  public BeansProvider beansProvider() {
    return new TestBeansProvider();
  }

  @MockBean
  public KeyValueRepository keyValRepo;

  @MockBean
  public FbMessageBuilder fbMsgBuilder;
}
```
## Transactions
https://docs.spring.io/spring/docs/5.0.9.RELEASE/spring-framework-reference/testing.html#testcontext-tx-enabling-transactions

Annotating a test method with @Transactional causes the test to be run within a transaction that will, by default, be automatically rolled back after completion of the test. If a test class is annotated with @Transactional, each test method within that class hierarchy will be run within a transaction. Test methods that are not annotated with @Transactional (at the class or method level) will not be run within a transaction. Furthermore, tests that are annotated with @Transactional but have the propagation type set to NOT_SUPPORTED will not be run within a transaction.

https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications

If your test is @Transactional, it rolls back the transaction at the end of each test method by default. However, as using this arrangement with either RANDOM_PORT or DEFINED_PORT implicitly provides a real servlet environment, the HTTP client and server run in separate threads and, thus, in separate transactions. Any transaction initiated on the server does not roll back in this case.

In case the test transaction should be committed in a Transactional method or class, add the following as first line of the method:

https://docs.spring.io/spring/docs/4.3.11.RELEASE/spring-framework-reference/htmlsingle/#testcontext-tx

```java
TestTransaction.flagForCommit();
```

## Override properties in tests

The standard properties file that Spring Boot picks up automatically when running an application is called application.properties and resides in the src/main/resources folder.

If we want to use different properties for tests, then we can override the properties file in the main folder by placing another file with the same name in src/test/resources.

## Using a separate datasource for testing

Another way to configure a separate DataSource for testing is by leveraging Spring Profiles to define a DataSource bean that is only available in a test profile.

Define a DataSource bean for the test profile in a @Configuration class that will be loaded the test:

```java
@Configuration
@EnableJpaRepositories(basePackages = {"org.labs.repository"})
@EnableTransactionManagement
public class H2TestProfileJPAConfig {
 
    @Bean
    @Profile("test")
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
        dataSource.setUsername("sa");
        dataSource.setPassword("sa");
 
        return dataSource;
    }
}
```

Then, in the JUnit test class, use the test profile by means of the @ActiveProfiles annotation:

```java
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, H2TestProfileJPAConfig.class})
@ActiveProfiles("test")
public class SpringBootProfileIntegrationTest {
    // ...
}
```

# Overriding a specific property through the SpringBootTest annotation

To load properties from the resources file and to override the values of some of them, use the properties attribute of @SpringBootTest:

```java
@RunWith(SpringRunner.class)
@SpringBootTest(
  webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT,
  classes={BotBackOfficeMainService.class, ChatHistoryTest.ChatCenterStoreTestConfig.class},
  properties={"agent-chat.map-closed-chats-prefix=TEST_map_closed_chats"})
@ActiveProfiles({"local"})
public class ChatHistoryTest {
  // ...
}
```

# Mocking beans

Beans can be mocked using the @MockBean annotation. We can specify test logic in the @Before method of the test. This is very useful for example, to simulate access to datasources via Repo Beans. 

Mocks can be registered by type or by bean name. Any existing single bean of the same type defined in the context will be replaced by the mock, if no existing bean is defined a new one will be added. When @MockBean is used on a field, as well as being registered in the application context, the mock will also be injected into the field.

```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"local"})
public class StandardServiceServiceImplTest {
  @MockBean
  private StandardServicesRepository standardServiceRepo;
  
  @Autowired
  private StandardServiceService stdServiceSrvc;
  
    @Before
  public void setup() {

    // Scenario 1: Bot 10. Has 3 BotAccounts 100, 101, 102. StandardServices enabled by BotAccount
    Mockito
      .when(standardServiceRepo.listStandardServicesByBotAccountId(100))
      .thenReturn(Collections.singletonList(new StandardService()));

    Mockito
      .when(standardServiceRepo.listStandardServicesByBotAccountId(101))
      .thenReturn(Collections.emptyList());
  }
  
  @Test
  public void testListStandardServicesForBotSession() {

    final int rootBotId = 1;
    final int bizProcessBotId = 11;
    final int botAccountId = 100;

    final List<StandardService> resStdServices =
      stdServiceSrvc.listStandardServicesForBotSession(rootBotId, botAccountId, bizProcessBotId);

    Assert.notNull(resStdServices, "Retrieved list of StandardServices is null");
  }
}
```

-- TODO: Add documentation about Mockito -------------------------------------------------------------------------------
https://www.baeldung.com/java-spring-mockito-mock-mockbean

"We can use the @MockBean to add mock objects to the Spring application context. The mock will replace any existing 
bean of the same type in the application context."

"If no bean of the same type is defined, a new one will be added. This annotation is useful in integration tests 
where a particular bean – for example, an external service – needs to be mocked."

Document what has been done at: 
botstandardservices com.vertical.bot.stdservices.bizlogic.state.ProcessSelectionTest
botbackoffice com.vertical.bot.backoffice.service.impl.StandardServiceServiceImplTest
botbackoffice com.vertical.bot.backoffice.domain.service.impl.UserStateServiceImplTest
------------------------------------------------------------------------------------------------------------------------




## References

https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html

https://docs.spring.io/spring/docs/5.0.8.RELEASE/spring-framework-reference/testing.html#testing

https://dzone.com/articles/spring-boot-unit-testing-and-mocking-with-mockito

https://dzone.com/articles/unit-and-integration-tests-in-spring-boot-2

https://www.baeldung.com/mockito-mock-methods

