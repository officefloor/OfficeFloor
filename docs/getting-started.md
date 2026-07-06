# Getting Started with the OfficeFloor Spring Boot Plugin

This gets you from zero to a running REST endpoint. Add one dependency to your `pom.xml`,
write one YAML file, and your endpoint is live. No controllers, no `@RequestMapping`, no Spring
MVC configuration.

Full tutorial source:
[SpringRestGettingStartedHttpServer](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestGettingStartedHttpServer)

## 1. Add the Maven dependency

The starter is published to Maven Central, so no extra repository configuration is needed. Add
the single dependency that matches your Spring Boot generation:

```xml
<!-- Spring Boot 4.x -->
<dependency>
  <groupId>net.officefloor.springboot</groupId>
  <artifactId>officefloor-rest-spring-boot-4-starter</artifactId>
  <version>4.0.2</version>
</dependency>

<!-- Spring Boot 3.x -->
<dependency>
  <groupId>net.officefloor.springboot</groupId>
  <artifactId>officefloor-rest-spring-boot-3-starter</artifactId>
  <version>4.0.2</version>
</dependency>
```

## 2. Application class — standard Spring Boot

The entry point is an ordinary `@SpringBootApplication` class with no OfficeFloor-specific code:

```java
@SpringBootApplication
public class SpringRestGettingStartedApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringRestGettingStartedApplication.class, args);
    }
}
```

## 3. Your first endpoint

An endpoint is a YAML file placed under `src/main/resources/officefloor/rest/`. The file name
encodes the HTTP method and the URL path:

```text
officefloor/rest/
└── greeting.GET.yml     →  GET  /greeting
```

The YAML file names the Java class that handles the request:

```yaml
service:
  class: net.officefloor.tutorial.springrestgettingstarted.GetGreetingLogic
```

Here `service` is a developer-chosen step name — it is not a keyword. The handler is a plain
Java class with no framework annotations on the class itself:

```java
public class GetGreetingLogic {

    public void service(GreetingService greetingService, ObjectResponse<GreetingResponse> response) {
        response.send(new GreetingResponse(greetingService.greet("World")));
    }
}
```

OfficeFloor registers every Spring bean in the application context as a managed object.
`GreetingService` is a plain Spring `@Service`, injected automatically by type into any service
method parameter whose type matches:

```java
@Service
public class GreetingService {

    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
```

`ObjectResponse<T>` serialises the object to JSON and writes it to the HTTP response — no
`@ResponseBody` or `@RestController` is needed.

## 4. Path parameters

A path variable in the URL is expressed by a curly-brace file or directory name:

```text
officefloor/rest/
└── greeting/
    └── {name}.GET.yml   →  GET  /greeting/{name}
```

```yaml
service:
  class: net.officefloor.tutorial.springrestgettingstarted.GetNamedGreetingLogic
```

The handler receives the path variable as a `@PathVariable` parameter. **Always use the
`name =` attribute form.** The shorthand `@PathVariable("name")` sets the `value` attribute;
OfficeFloor resolves arguments from raw Java reflection where `@AliasFor` synthesis is not
applied, so the shorthand silently produces an empty name and the binding fails.

```java
public class GetNamedGreetingLogic {

    public void service(
            @PathVariable(name = "name") String name,
            GreetingService greetingService,
            ObjectResponse<GreetingResponse> response) {
        response.send(new GreetingResponse(greetingService.greet(name)));
    }
}
```

## 5. Run it

Because the application is standard Spring Boot, it can be run directly:

```bash
mvn spring-boot:run
```

```bash
curl http://localhost:8080/greeting
{"message":"Hello, World!"}

curl http://localhost:8080/greeting/OfficeFloor
{"message":"Hello, OfficeFloor!"}
```

## 6. Testing

The application is a standard Spring Boot application, so tests use `MockMvc`, or
`@SpringBootTest(webEnvironment = RANDOM_PORT)` with `TestRestTemplate` for real HTTP calls
against an embedded server.

## Next

* [YAML Endpoint Configuration](yaml-endpoint-configuration.md) — naming conventions, multi-step flows, branching, escalations and governance
* [Spring Integration](spring-integration.md) — bean injection, Spring MVC annotations, `ResponseEntity`
