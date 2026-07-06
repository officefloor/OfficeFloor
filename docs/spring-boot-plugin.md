# OfficeFloor Spring Boot Plugin

**Explicit, AI-friendly YAML orchestration for Spring Boot REST endpoints.**

OfficeFloor is a Spring Boot add-on. It adds explicit YAML-based function orchestration
alongside your existing Spring beans, security, persistence and controllers. Spring's
dependency injection keeps doing what it does; OfficeFloor makes the wiring between the
steps of an endpoint visible in a single file rather than scattered across annotations and
framework conventions.

Website and tutorials: [https://officefloor.net](https://officefloor.net)

## Why this is more AI-friendly than raw Spring

A Spring `@RestController` that handles validation, business logic and auditing in one class
works fine, but the *flow* between those concerns is implicit. It lives in the framework's
call stack and Spring's wiring rules, not in any single readable artefact. That opacity costs
time when reasoning about an endpoint, and it limits how reliably AI coding tools can read or
generate endpoint code.

OfficeFloor introduces an explicit YAML file per endpoint that declares the function steps,
their order, and how outputs connect — while each function class continues to use Spring beans
via normal injection. The full structure of an endpoint is explicit in one file, so an AI (or a
human) can read, generate and refactor endpoints from the YAML alone, without tracing implicit
framework behaviour across multiple Java files.

```yaml
# File: src/main/resources/officefloor/rest/greeting.POST.yml
# Mapped to: POST /greeting

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
complete specification of the endpoint: its steps, their order, and their conditional branches,
all readable without opening a single Java file.

## Progressive adoption — it enriches Spring, it does not replace it

Add a single dependency to your existing Spring Boot `pom.xml` — choose the starter that
matches your Spring Boot generation (see [Version-specific starters](#version-specific-starters)
below). For a Spring Boot 4.x application:

```xml
<dependency>
  <groupId>net.officefloor.springboot</groupId>
  <artifactId>officefloor-rest-spring-boot-4-starter</artifactId>
  <version>4.0.2</version>
</dependency>
```

The starter auto-configures OfficeFloor into the Spring MVC pipeline. On start-up it scans the
classpath for YAML files under `officefloor/rest/` and registers each one as a handler for the
corresponding HTTP method and URL path. No additional Java or XML configuration is required.

Spring's dependency injection, security, persistence and actuator configuration remain
completely intact. You can start declaring endpoints as YAML files alongside your existing
`@RestController` classes and migrate incrementally.

## Version-specific starters

Two starters are published — add only the one matching your Spring Boot generation. Mixing
versions can cause `NoSuchMethodError` or other binary incompatibilities at runtime.

| Spring Boot version | Starter artifact |
| --- | --- |
| Spring Boot 3.x | `officefloor-rest-spring-boot-3-starter` |
| Spring Boot 4.x | `officefloor-rest-spring-boot-4-starter` |

## The underlying paradigm: Inversion of Coupling Control

The paradigm behind OfficeFloor separates three concerns that most frameworks conflate:

* **Continuation Injection** — injecting functions to orchestrate application behaviour (what the YAML files express)
* **Thread Injection** — injecting the thread (pool) to execute a particular function
* **Dependency Injection** — injecting objects for state into functions (this is what Spring already provides)

Explicit YAML orchestration is the practical expression of Continuation Injection applied to
REST endpoints. Read more in the paper
[OfficeFloor: using office patterns to improve software design](http://doi.acm.org/10.1145/2739011.2739013)
or the [introductory blog post](https://sagenschneider.blogspot.com/2019/02/inversion-of-coupling-control.html).

## Where to go next

* [Getting Started](getting-started.md) — from zero to a running endpoint with one dependency and one YAML file
* [YAML Endpoint Configuration](yaml-endpoint-configuration.md) — the full endpoint model reference
* [Spring Integration](spring-integration.md) — bean injection, Spring MVC annotations, responses
* [Tutorials](tutorials.md) — the full Spring Boot tutorial series
