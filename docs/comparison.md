# `@RestController` vs OfficeFloor YAML: the same endpoint, two ways

OfficeFloor does not replace Spring MVC. It adds an explicit YAML orchestration layer *on top of*
your existing Spring Boot application, using the same beans, the same security, the same
persistence. Two things change, and both make an endpoint easier for a developer or an AI tool to
work with:

1. **How you find the code behind a URL.** OfficeFloor's YAML files sit in a directory tree that
   mirrors the URL structure, so the filesystem is a direct index from URL to code. With Spring the
   route is declared in an annotation that can live in any class in any package.
2. **Where an endpoint's flow lives.** Having reached that one file, it names every step and class
   in the endpoint. In a `@RestController` the flow between validation, business logic and
   persistence is implicit in one method's call stack.

Together these give a clean drill-down: navigate from a URL straight to a single file, and from that
file straight into the exact code involved, with all of its context. The two sections below follow
those two points in order.

> **Not a competition.** Everything below runs inside Spring Boot. Dependency injection, Spring
> Security, Spring Data and Actuator behave identically. OfficeFloor is a progressive add-on: you
> can keep your `@RestController` classes and introduce YAML endpoints alongside them.

## 1. Navigating from a URL to the code

Given a URL such as `GET /greeting/{name}`, how do you, or an AI tool, find the code that handles
it?

**With OfficeFloor the URL *is* the path on disk.** Directory nesting mirrors the path segments,
`{curly}` names become path variables, and the suffix (`.GET`, `.POST`) is the HTTP method. The
`officefloor/rest/` tree reads as a table of contents for the whole API:

```
src/main/resources/officefloor/rest/
  greeting.GET.yml         ->  GET  /greeting
  greeting/
    {name}.GET.yml         ->  GET  /greeting/{name}
  order.POST.yml           ->  POST /order
```

An assistant resolves a URL in two deterministic hops, with no searching required:

1. **URL to file.** Map the URL by path to its `.yml` file. No annotation parsing, no call-graph
   analysis. Every route in the application can be enumerated just by listing the tree above.
2. **File to code.** That file names every step and handler class in the endpoint's flow (shown in
   the next section). The assistant loads exactly those classes, and no others, giving it the
   complete and minimal context for the endpoint.

**With `@RestController` there is no positional relationship between the URL and the code.** The
handler can live in any class, in any package. The route is often spread across a class-level
`@RequestMapping` prefix and a method-level `@GetMapping` suffix that must be concatenated to
recover the full path. Resolving a URL means scanning the controller layer and parsing annotations;
understanding the endpoint then means following the call stack out of the controller method to
discover which collaborators are involved. An AI assistant has to ingest and reason over a large
amount of unrelated code just to find where a route is declared and what it touches.

This is the difference that matters most for AI-assisted development. The project layout itself is
contextual grounding: routes are enumerable by listing a directory, and the code for any route is
one deterministic file lookup away. As a codebase grows to hundreds of endpoints, that difference
compounds.

## 2. Orchestration: the endpoint's flow in one file

Once you have navigated to the file, it is the map into the code. This section shows what that file
holds, from the simplest endpoint to a multi-step flow.

### Case 1: a simple endpoint (`GET /greeting`)

For a single-step endpoint the two approaches are almost identical. There is little flow to make
explicit, so the value of orchestration is small.

#### With `@RestController`

```java
@RestController
public class GreetingController {

    private final GreetingService greetingService;

    public GreetingController(GreetingService greetingService) {
        this.greetingService = greetingService;
    }

    @GetMapping("/greeting")
    public GreetingResponse greeting() {
        return new GreetingResponse(greetingService.greet("World"));
    }
}
```

#### With OfficeFloor

```yaml
# src/main/resources/officefloor/rest/greeting.GET.yml
service:
  class: GetGreetingLogic
```

```java
public class GetGreetingLogic {

    public void service(GreetingService greetingService, ObjectResponse<GreetingResponse> response) {
        response.send(new GreetingResponse(greetingService.greet("World")));
    }
}
```

Same `GreetingService` bean, injected the same way. The file name `greeting.GET.yml` encodes the
method and path, so no `@GetMapping` is needed. For an endpoint this small, that is the only
difference worth noting.

### Case 2: a multi-step endpoint (`POST /order`)

The picture changes when an endpoint has real flow: validate the request, branch on the result,
price the order, then persist it. This is where the orchestration becomes visible.

#### With `@RestController`

The flow lives inside one method. The order of steps, the validation branch, and how data passes
from one step to the next are all implicit in the Java control flow:

```java
@RestController
public class OrderController {

    private final PricingService pricingService;
    private final OrderService orderService;

    public OrderController(PricingService pricingService, OrderService orderService) {
        this.pricingService = pricingService;
        this.orderService = orderService;
    }

    @PostMapping("/order")
    public OrderResponse order(@RequestBody OrderRequest request) {

        // validate
        if (request.getProductId() == null || request.getProductId().isBlank()
                || request.getQuantity() <= 0) {
            return new OrderResponse(null, request.getProductId(), request.getQuantity(), 0.0);
        }

        // price
        double total = pricingService.calculateTotal(request.getProductId(), request.getQuantity());

        // save
        String orderId = orderService.createOrder(request.getProductId(), request.getQuantity(), total);
        return new OrderResponse(orderId, request.getProductId(), request.getQuantity(), total);
    }
}
```

To understand this endpoint you read the method top to bottom and reconstruct the flow in your
head. As it grows, with more branches, error handling and cross-cutting concerns, that
reconstruction gets harder. It is exactly the part an AI tool cannot see from annotations alone.

#### With OfficeFloor

The flow is declared in the YAML file. Each step is a small, independent handler class:

```yaml
# src/main/resources/officefloor/rest/order.POST.yml
validate:
  class: ValidateOrderLogic
  outputs:
    valid: price      # only the "valid" branch continues

price:
  class: CalculatePricingLogic
  next: save          # pass the priced order on

save:
  class: SaveOrderLogic
```

```java
public class ValidateOrderLogic {

    @FunctionalInterface
    public interface ValidOrderFlow {
        void flow(OrderRequest order);
    }

    public void service(
            @RequestBody OrderRequest request,
            @Flow("valid") ValidOrderFlow validFlow,
            ObjectResponse<OrderResponse> response) {
        if (request.getProductId() == null || request.getProductId().isBlank()
                || request.getQuantity() <= 0) {
            response.send(new OrderResponse(null, request.getProductId(), request.getQuantity(), 0.0));
        } else {
            validFlow.flow(request);   // routes to the "valid" -> price step
        }
    }
}
```

```java
public class CalculatePricingLogic {

    public PricedOrder price(@Parameter OrderRequest order, PricingService pricingService) {
        double total = pricingService.calculateTotal(order.getProductId(), order.getQuantity());
        return new PricedOrder(order.getProductId(), order.getQuantity(), total);   // becomes next step's @Parameter
    }
}
```

```java
public class SaveOrderLogic {

    public void save(@Parameter PricedOrder order, OrderService orderService,
            ObjectResponse<OrderResponse> response) {
        String orderId = orderService.createOrder(order.getProductId(), order.getQuantity(), order.getTotal());
        response.send(new OrderResponse(orderId, order.getProductId(), order.getQuantity(), order.getTotal()));
    }
}
```

The YAML file is the complete, readable specification of the endpoint: its steps, their order, and
the conditional branch, all in one place. It also names the exact set of classes involved, so from
this one file both a developer and an AI tool know precisely which code to open for full context.
Each handler knows only its own inputs and its own Spring beans. None of them knows about the
others.

## What stays exactly the same

| Concern | `@RestController` | OfficeFloor YAML |
| --- | --- | --- |
| Dependency injection | Spring beans injected by type | Spring beans injected by type (identical) |
| Spring Security | Unchanged | Unchanged |
| Spring Data / persistence | Unchanged | Unchanged |
| Bean Validation, Actuator, OpenAPI | Unchanged | Unchanged |
| Request/response binding | `@RequestBody`, `@PathVariable`, `@RequestParam` | The same annotations are supported |

## What changes

| Aspect | `@RestController` | OfficeFloor YAML |
| --- | --- | --- |
| Finding the code for a URL | Scan and parse annotations across many classes | List a directory; open the matching file path |
| Enumerating all routes | Static analysis of every controller | List the `officefloor/rest/` tree |
| Knowing which classes an endpoint touches | Follow the call stack out of the controller | The file names every class in the flow |
| Where the flow lives | Implicit in the method's call stack | Explicit in the `.yml` file |
| Routing | `@GetMapping` / `@PostMapping` | Encoded in the file name and directory path |
| Branching between steps | `if` / method calls in Java | `outputs:` map in YAML |
| Passing data downstream | Local variables | Return value becomes next step's `@Parameter` |
| Error routing | `@ControllerAdvice`, try/catch | `escalations:` (unmatched fall through to `@ControllerAdvice`) |
| AI reads / generates the flow | Must infer from annotations plus call stack | Reads and generates from one file |

## When to use which

- **Reach for a `@RestController`** when an endpoint is a single step with little internal flow.
  The YAML adds ceremony without buying much explicitness (see Case 1).
- **Reach for OfficeFloor YAML** when you want endpoints that are navigable from the URL alone and
  whose flow is explicit: multiple steps, conditional branches, shared error handling, or
  cross-cutting concerns (governance and transactions). This is also what lets AI tooling jump from
  a URL to the exact code with full context.
- **You do not have to choose globally.** Both live in the same application. Migrate the endpoints
  that benefit and leave the rest.

## Next steps

- [Getting started](getting-started.md): one dependency, one YAML file, from zero to a running endpoint.
- [YAML endpoint configuration](yaml-endpoint-configuration.md): the full reference for steps, `next:`/`outputs:`, escalations and governance.
- [Spring REST to OfficeFloor conversion reference](https://officefloor.net/tutorials/springboot/SpringRestConversionReference/index.html): the mechanical substitutions to convert a `@RestController` into YAML composition.
- [Tutorial series](https://officefloor.net/tutorials/index.html): complete runnable examples for each capability.
