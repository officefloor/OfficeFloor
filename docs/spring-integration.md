# Spring Integration

OfficeFloor handler classes are plain Java. They obtain Spring beans and HTTP data using
standard Spring dependency injection and Spring MVC parameter annotations. OfficeFloor enriches
Spring; it does not replace it.

## Bean injection — parameters injected by type

OfficeFloor registers every bean in the Spring application context as a managed object. Any
parameter of a service method whose type matches a Spring bean is injected automatically — no
annotation is needed on that parameter.

```java
@Service
public class GreetingService {
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
```

```java
public class GetGreetingLogic {
    public void service(GreetingService greetingService, ObjectResponse<GreetingResponse> response) {
        response.send(new GreetingResponse(greetingService.greet("World")));
    }
}
```

The class carries no `@RestController`, `@RequestMapping` or `@ResponseBody`. The YAML file wires
the class to a URL and HTTP method (see
[YAML Endpoint Configuration](yaml-endpoint-configuration.md)).

## Spring MVC parameter annotations

Service methods can use all the standard Spring MVC parameter annotations, plus OfficeFloor's own
web annotations:

* Spring: `@PathVariable`, `@RequestParam`, `@RequestHeader`, `@CookieValue`, `@RequestBody`, `@ModelAttribute`, `@RequestPart`
* OfficeFloor: `@HttpPathParameter`, `@HttpQueryParameter`, `@HttpHeaderParameter`, `@HttpObject`

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

### Always use the `name =` attribute form

Use `@PathVariable(name = "name")` and `@RequestParam(name = "name")`. The shorthand
`@PathVariable("name")` sets the `value` attribute, which requires `@AliasFor` annotation
synthesis to alias to `name`. OfficeFloor resolves arguments from raw Java reflection where that
synthesis is not applied, so the shorthand **silently produces an empty value and the binding
fails.**

## Responses — `ObjectResponse<T>`

`ObjectResponse<T>` serialises the object to JSON and writes it to the HTTP response. Declare it
as a method parameter and call `send(...)`:

```java
response.send(new GreetingResponse(...));
```

### Custom headers and status — `ObjectResponse<ResponseEntity<T>>`

For full Spring compatibility, `ObjectResponse` also accepts a `ResponseEntity<T>` as its type
parameter. This lets you set custom response headers or a non-200 status code while keeping the
same dependency-injected style:

```java
public void service(ObjectResponse<ResponseEntity<GreetingResponse>> response) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("X-Greeting", "custom");
    response.send(new ResponseEntity<>(new GreetingResponse("Hello"), headers, HttpStatus.OK));
}
```

When you only need the body and a 200 status, `ObjectResponse<T>` is simpler; reach for the
`ResponseEntity` form when you need headers or a specific status code.

## What stays exactly the same as any Spring Boot app

* `@SpringBootApplication` entry point
* Spring Security, Spring Data JPA, Bean Validation, Actuator, Thymeleaf and other Spring Boot starters
* `@Service`, `@Component`, `@Repository`, `@Configuration` beans and `@Qualifier` injection
* Testing with `MockMvc` and `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate`

See the [Tutorials](tutorials.md) for worked examples of each of these integrations.
