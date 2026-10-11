# OfficeFloor Spring Boot Plugin

**Let AI add feature after feature, and your code stays tidy. Explicit Function Injection for Spring Boot REST endpoints.**

OfficeFloor is a Spring Boot add-on. Let an AI keep adding features to a normal codebase and it
slowly erodes into a tangle. OfficeFloor stops that. Each endpoint is one YAML file that lists
its steps in order. Each step is one small Java class. Spring's dependency injection keeps doing
what it does. OfficeFloor adds the orchestration on top.

Website and tutorials: [https://officefloor.net](https://officefloor.net)

## Why Function Injection stops AI eroding your code

A Spring `@RestController` that handles validation, business logic and auditing in one class
works fine at first. Then you ask an AI to add a feature. Then another. Each one piles more code
into the same method. The method grows. The logic tangles. Bit by bit the code erodes. Soon the
AI has to understand the whole tangle just to change one small thing. That is when it starts
breaking what already worked.

OfficeFloor keeps each step separate. An endpoint is one YAML file that lists the steps in order.
Each step is one small Java class that does one job. To add a feature you add a step. That means
one small new class and one new line in the file. Everything already there stays exactly as it
was. There is only one place each piece of logic can go, so an AI cannot scatter it around. An AI
still writes and changes these endpoints for you. It just cannot erode them.

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
complete specification of the endpoint. Its steps, their order, and their conditional branches
are all declared in one file.

## Progressive adoption: it enriches Spring, it does not replace it

Add a single dependency to your existing Spring Boot `pom.xml`. Choose the starter that
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

Two starters are published. Add only the one matching your Spring Boot generation. Mixing
versions can cause `NoSuchMethodError` or other binary incompatibilities at runtime.

| Spring Boot version | Starter artifact |
| --- | --- |
| Spring Boot 3.x | `officefloor-rest-spring-boot-3-starter` |
| Spring Boot 4.x | `officefloor-rest-spring-boot-4-starter` |

## The underlying paradigm: Inversion of Coupling Control

The paradigm behind OfficeFloor separates three concerns that most frameworks conflate:

* **Continuation Injection**: injecting functions to orchestrate application behaviour. This is what the YAML files express. It is the part Spring does not provide.
* **Dependency Injection**: injecting objects for state into functions. Spring already provides this.
* **Thread Injection**: injecting the thread (pool) to execute a particular function. Java 21 and 25 virtual threads now cover the common case, so this is mostly a tuning option.

Explicit YAML orchestration is the practical expression of Continuation Injection applied to
REST endpoints. Read more in the paper
[OfficeFloor: using office patterns to improve software design](http://doi.acm.org/10.1145/2739011.2739013)
or the [introductory blog post](https://sagenschneider.blogspot.com/2019/02/inversion-of-coupling-control.html).

## Where to go next

* [Getting Started](getting-started.md): from zero to a running endpoint with one dependency and one YAML file
* [YAML Endpoint Configuration](yaml-endpoint-configuration.md): the full endpoint model reference
* [Spring Integration](spring-integration.md): bean injection, Spring MVC annotations, responses
* [Tutorials](tutorials.md): the full Spring Boot tutorial series
