
 [![Website](https://img.shields.io/website-up-down-green-red/http/officefloor.net.svg?label=http://officefloor.net)](http://officefloor.net)

 ![Continuous Integration](https://github.com/officefloor/OfficeFloor/workflows/Continuous%20Integration/badge.svg)
 [![codecov](https://codecov.io/gh/officefloor/OfficeFloor/branch/master/graph/badge.svg)](https://codecov.io/gh/officefloor/OfficeFloor)

 [![Maven Central](https://img.shields.io/maven-central/v/net.officefloor/officefloor.svg)](https://search.maven.org/search?q=a:officefloor)
 ![GitHub](https://img.shields.io/github/license/officefloor/OfficeFloor)

# OfficeFloor

**Explicit YAML orchestration for AI-augmented Spring Boot REST**

OfficeFloor is a Spring Boot add-on. It adds explicit YAML-based function orchestration alongside your existing Spring beans, security, persistence, and controllers. Spring's dependency injection keeps doing what it does; OfficeFloor makes the wiring between endpoint steps visible in one file rather than scattered across annotations and framework conventions.

More information and tutorials at [http://officefloor.net](http://officefloor.net)


## What it adds to Spring

A Spring `@RestController` that handles validation, business logic, and auditing in one class works fine, but the flow between those concerns is implicit. It lives in the framework's call stack and Spring's wiring rules, not in any single readable artefact. That opacity costs time when reasoning about an endpoint, and it limits how reliably AI coding tools can read or generate endpoint code.

OfficeFloor introduces an explicit YAML file per endpoint that declares the function steps, their order, and how outputs connect, while each function class continues to use Spring beans via normal injection.


## Explicit YAML orchestration

The file name encodes the HTTP method and URL path. The file body declares each function step, its class, and how outputs connect to the next step:

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

Each function class declares only its own Spring bean dependencies, injected by Spring exactly as they would be in any other bean. No function knows about the others. The YAML file is the complete specification of the endpoint: its steps, their order, and their conditional branches, all readable without opening a single Java file.

This makes endpoints reliable targets for AI coding tools: the full structure is explicit in one file, so an AI can read, generate, and refactor endpoints from the YAML alone.


## Progressive adoption

Add a single dependency to your existing Spring Boot `pom.xml`, choosing the starter that matches your Spring Boot generation:

```xml
<!-- Spring Boot 4.x -->
<dependency>
  <groupId>net.officefloor.springboot</groupId>
  <artifactId>officefloor-rest-spring-boot-4-starter</artifactId>
  <version>4.0.2</version>
</dependency>

<!-- Spring Boot 3.x: use officefloor-rest-spring-boot-3-starter instead -->
```

Add only the starter matching your Spring Boot generation — mixing versions causes runtime binary incompatibilities.

Spring's dependency injection, security, persistence, and actuator configuration remain completely intact. OfficeFloor enriches Spring, it does not replace it. You can start declaring endpoints as YAML files alongside your existing `@RestController` classes and migrate incrementally.


## Inversion of Coupling Control

The underlying paradigm behind OfficeFloor separates three concerns that most frameworks conflate:

* **Continuation Injection**: injecting functions to orchestrate application behaviour (what the YAML files express)
* **Thread Injection**: injecting the thread (pool) to execute a particular function
* **Dependency Injection**: injecting objects for state into functions

Explicit YAML orchestration is the practical expression of Continuation Injection applied to REST endpoints. Read more in the paper [OfficeFloor: using office patterns to improve software design](http://doi.acm.org/10.1145/2739011.2739013) or the [introductory blog post](https://sagenschneider.blogspot.com/2019/02/inversion-of-coupling-control.html).


## Getting started

See the [tutorial series](http://officefloor.net/tutorials/index.html) for step-by-step guides covering endpoint composition, conditional branching, error flows, Spring integration, and more.
