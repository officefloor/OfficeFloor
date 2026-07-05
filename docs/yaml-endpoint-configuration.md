# YAML Endpoint Configuration Reference

This is the complete model for declaring Spring Boot REST endpoints as OfficeFloor YAML files.
Every endpoint's structure — its steps, their order, and their conditional branches — is
explicit in one file, which is what makes endpoints reliable targets for AI coding tools.

## File location and naming — path and method from the file name

Endpoints are YAML files placed under `src/main/resources/officefloor/rest/`. The file name
encodes both the HTTP method and the URL path. The naming convention is `{path}.{METHOD}.yml`:

```text
officefloor/rest/
├── greeting.GET.yml             →  GET  /greeting
└── greeting/
    ├── {name}.GET.yml           →  GET  /greeting/{name}
    ├── entity/
    │   └── {name}.GET.yml       →  GET  /greeting/entity/{name}
    ├── formal/
    │   └── {name}.GET.yml       →  GET  /greeting/formal/{name}
    └── {style}/
        └── {name}.GET.yml       →  GET  /greeting/{style}/{name}
```

Rules:

* Directory structure below `officefloor/rest/` becomes the URL path — deeper URLs are produced by nesting files in sub-directories.
* Curly-brace segments such as `{name}` become URL path parameters.
* The special filename `index.{METHOD}.yml` maps to the root path `/`.

On start-up the starter scans the classpath for these YAML files and registers each one as a
handler for its HTTP method and URL path. No additional Java or XML configuration is needed.

## Steps — entries are named, the first is the entry point

Inside each YAML file, top-level entries are named steps. The label on each entry is a
developer-chosen name used to wire steps together — **it is not a keyword.** A step identifies
the Java class that implements it:

```yaml
myLabel:
  class: com.example.MyLogic
```

The **first entry** in the file is always called when the HTTP request arrives.

When a class has only one public method, that method is used automatically.

## `method:` — required for multi-method classes

When a class has more than one public method, OfficeFloor cannot determine which to call and the
application fails to start with:

```text
Require configuring method for service (GreetingStyleLogic) as it contains
multiple public methods (casual, formal)
```

Every YAML entry that references such a class must include `method:` to name which method to
invoke:

```yaml
# greeting/formal/{name}.GET.yml
service:
  class: net.officefloor.tutorial.springresthttpserver.GreetingStyleLogic
  method: formal
```

```yaml
# greeting/casual/{name}.GET.yml
service:
  class: net.officefloor.tutorial.springresthttpserver.GreetingStyleLogic
  method: casual
```

Both entries reference the same class but each picks a different method.

## `next:` — an unconditional next step

Use `next:` to chain to the next step unconditionally after the current step completes:

```yaml
service:
  class: net.officefloor.tutorial.catshttpserver.ServiceLogic
  method: service
  next: send

send:
  class: net.officefloor.tutorial.catshttpserver.ServiceLogic
  method: send
```

### How data flows to a `next:` step in code

The value a handler method **returns** becomes the input to the `next:` step. The receiving
method declares a parameter annotated with `@Parameter` (from
`net.officefloor.plugin.section.clazz.Parameter`) to receive it. Returning a value plus `next:`
is the lightweight way to pass data downstream when there is no branching:

```java
// The step with `next: save` returns a PricedOrder ...
public class CalculatePricingLogic {
    public PricedOrder price(@Parameter OrderRequest order, PricingService pricingService) {
        double total = pricingService.calculateTotal(order.getProductId(), order.getQuantity());
        return new PricedOrder(order.getProductId(), order.getQuantity(), total);
    }
}

// ... and the `save` step receives it as an @Parameter
public class SaveOrderLogic {
    public void save(@Parameter PricedOrder order, OrderService orderService,
                     ObjectResponse<OrderResponse> response) {
        String orderId = orderService.createOrder(order.getProductId(), order.getQuantity(), order.getTotal());
        response.send(new OrderResponse(orderId, order.getProductId(), order.getQuantity(), order.getTotal()));
    }
}
```

Only the type matters for the wiring: the return type of one step is matched to the `@Parameter`
type of the next.

## `outputs:` — conditional branches

A step may declare named outputs. Each output maps a branch name to the step to run when the
handler triggers that output — enabling conditional flow:

```yaml
validate:
  class: net.officefloor.tutorial.springrestfunction.ValidateOrderLogic
  outputs:
    valid: price

price:
  class: net.officefloor.tutorial.springrestfunction.CalculatePricingLogic
  next: save

save:
  class: net.officefloor.tutorial.springrestfunction.SaveOrderLogic
```

Here `validate` continues to `price` only via its `valid` output; `price` then always continues
to `save`. The whole flow — validate, then price, then save — is readable without opening any
Java file.

### How an output is defined and triggered in code — `@Flow`

The output name in the YAML (`valid`) is not magic — it is matched to a **flow** declared in the
handler. A flow is a custom `@FunctionalInterface` parameter annotated with `@Flow` (from
`net.officefloor.plugin.section.clazz.Flow`), where the annotation value is the output name. The
handler *triggers* the branch by calling the interface's method; the argument passed becomes the
`@Parameter` of the receiving step:

```java
public class ValidateOrderLogic {

    // Custom functional interface = the "valid" branch. Its argument type (OrderRequest)
    // becomes the @Parameter of the target step (price).
    @FunctionalInterface
    public interface ValidOrderFlow {
        void flow(OrderRequest order);
    }

    public void service(
            @RequestBody OrderRequest request,
            @Flow("valid") ValidOrderFlow validFlow,       // maps to `outputs: { valid: price }`
            ObjectResponse<OrderResponse> response) {
        if (request.getProductId() == null || request.getProductId().isBlank()
                || request.getQuantity() <= 0) {
            // Invalid: respond directly and short-circuit — `price`/`save` never run.
            response.send(new OrderResponse(null, request.getProductId(), request.getQuantity(), 0.0));
        } else {
            // Valid: route to whatever step `valid` is mapped to in the YAML (here, price).
            validFlow.flow(request);
        }
    }
}
```

Key points:

* `@Flow("valid")` binds the parameter to the YAML output named `valid`; the class name of the functional interface (`ValidOrderFlow`) is arbitrary.
* Calling `validFlow.flow(request)` transfers execution to the mapped step. The argument (`request`) arrives there as an `@Parameter`.
* A step can declare several `@Flow` parameters for several outputs, and simply not call the ones whose branches should not run — that is how conditional and short-circuit routing is expressed.
* This keeps each class ignorant of the others: `ValidateOrderLogic` never names `CalculatePricingLogic`. The YAML `outputs:` map is the single place the wiring lives.

## `escalations:` — exception handling

Exceptions (called *escalations* in OfficeFloor) are handled with the **same function-injection
model** as `outputs:` and `next:`: a handler is a plain Java class whose method receives the
routed value as an `@Parameter` and writes the response with `ObjectResponse`. The one
difference is *how the branch is triggered* — a step does not call a `@Flow` method, it simply
**throws the exception**, and OfficeFloor routes it to the matching handler.

### The exception must be checked (`extends Exception`)

For OfficeFloor to discover and route an escalation, the exception must be a **checked**
exception so that it appears in the method's `throws` clause. That `throws` clause is how the
YAML wiring is validated at start-up:

```java
public class MockException extends Exception {
    public MockException(String message) {
        super(message);
    }
}
```

The service step just declares and throws it — it names no handler:

```java
public class MethodService {
    public void service() throws MockException {
        throw new MockException("thrown");
    }
}
```

### The handler receives the exception as `@Parameter`

The thrown exception is passed to the handler exactly like any other step input — via
`@Parameter` (from `net.officefloor.plugin.section.clazz.Parameter`). This is the same
annotation used to receive a `next:` return value or a `@Flow` argument; for an escalation the
value is the thrown exception object:

```java
public class MethodExceptionHandler {
    public void handle(@Parameter MockException ex, ObjectResponse<String> response) {
        response.send("Method handled: " + ex.getMessage());
    }
}
```

No Spring-specific annotations are needed. The handler can return a `ResponseEntity` (via
`ObjectResponse<ResponseEntity<T>>`) to set the HTTP status and a `ProblemDetail` body.

### Three levels of routing (highest precedence first)

The Java classes are written identically regardless of which level catches the exception — the
level is chosen entirely in YAML.

**1. Method escalation** — declared under the step that throws, applies to that step only:

```yaml
service:
  class: net.officefloor.tutorial.springrestexceptionhttpserver.MethodService
  escalations:
    net.officefloor.tutorial.springrestexceptionhttpserver.MockException: handler

handler:
  class: net.officefloor.tutorial.springrestexceptionhttpserver.MethodExceptionHandler
```

**2. Composition escalation** — declared in a `composition:` block at the top of the file,
applies to every step in that file:

```yaml
composition:
  escalations:
    net.officefloor.tutorial.springrestexceptionhttpserver.MockException: handler

service:
  class: net.officefloor.tutorial.springrestexceptionhttpserver.CompositionService

handler:
  class: net.officefloor.tutorial.springrestexceptionhttpserver.CompositionExceptionHandler
```

**3. Global escalation** — application-wide, one file per exception type under
`officefloor/escalation/`, the file name being the fully qualified exception class name. Endpoint
YAMLs need no escalation config; the global handler wires automatically. This is the preferred
OfficeFloor-native replacement for Spring's `@RestControllerAdvice`:

```yaml
# File: officefloor/escalation/com.example.EscalationNotFoundException.yml
handle:
  class: com.example.GlobalExceptionHandler
  method: handleNotFound
```

When one handler class serves several exception types, use `method:` in each escalation file to
pick the method. Global escalation also catches exceptions thrown by governance (e.g. a
`TransactionSystemException` at transaction commit), since governance failures route through the
same mechanism.

**Precedence:** method escalation → composition escalation → global escalation. An endpoint can
always override a global handler by declaring its own.

### Fall-through to Spring `@RestControllerAdvice`

If no method, composition, or global escalation matches, the exception propagates out of the
OfficeFloor composition and is handled by Spring's standard
`@RestControllerAdvice` / `@ExceptionHandler` infrastructure. This lets OfficeFloor endpoints
participate in an existing Spring application's exception handling with no extra config. Prefer
global escalation for new applications; use the Spring fall-through when integrating with
existing `@ControllerAdvice` handlers.

## `govern:` — cross-cutting concerns

Wrap a step's execution with governance — such as a database transaction or auditing — using a
`govern:` list. Governance is defined once (under `officefloor/govern/`) and applied per step:

```yaml
# apply a transaction around the step
service:
  class: net.officefloor.tutorial.springrestdatajpa.CreateArticleService
  govern: [ transaction ]
```

```yaml
# apply audit governance around the step
service:
  class: net.officefloor.tutorial.springrestgovernance.GovernedService
  govern: [ audit ]
```

## Other configuration folders

Alongside `officefloor/rest/`, endpoints can draw on:

* `officefloor/escalation/` — global exception handlers, named by exception class
* `officefloor/govern/` — governance definitions referenced by `govern:`
* `officefloor/managedobjects/` — custom managed object state sources

## Putting it together

```yaml
# src/main/resources/officefloor/rest/greeting.POST.yml  →  POST /greeting

validate:
  class: ValidateGreetingLogic
  outputs:
    valid: build

build:
  class: PostGreetingLogic
  next: audit

audit:
  class: AuditGreetingLogic
```

Each function class declares only its own Spring bean dependencies, injected by Spring exactly
as they would be in any other bean. No function knows about the others. The YAML file is the
complete specification of the endpoint.

See [Spring Integration](spring-integration.md) for how the handler methods obtain Spring beans
and use Spring MVC parameter annotations.
