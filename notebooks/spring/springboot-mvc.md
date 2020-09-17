## Spring MVC Application

By default, Spring Boot looks for templates and static contents at:

```bash
src/main/resources/templates
src/main/resources/static
```

Enable Spring Boot Devtools, which automatically relaods classes as they are modified, so that changes in the backend code have immediate effect.

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-devtools</artifactId>
  <scope>runtime</scope>
  <optional>true</optional>
</dependency>
```

Templates and static contents are a bit trickier, though. Make sure the following properties to the development environment, so that changes on tempaltes and static files have effect without restarting the server.
For example, in application.yaml:

```yaml
server:
  port: 8081
---
spring:
  profiles: development
  thymeleaf:
    # Ensure that changes on templates have immediate effect during development
    prefix: "file://@project.basedir@/src/main/resources/templates/"
    cache: false
  resources:
    # Ensure that changes on static contents have immediate effect during development
    static-locations: "file://@project.basedir@/src/main/resources/static/"
```

### References

- [Spring Boot - Integrating Static Content](https://www.springboottutorial.com/spring-boot-with-static-content-css-and-javascript-js)
- [Accessing Spring MVC Model Objects in JS](https://www.baeldung.com/spring-mvc-model-objects-js)
- [Spring MVC - Add JS, CSS and images into JSP file using mvc:resources mapping](https://crunchify.com/spring-mvc-4-2-2-best-way-to-integrate-js-and-css-file-in-jsp-file-using-mvcresources-mapping/)
