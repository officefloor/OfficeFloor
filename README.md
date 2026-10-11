
 [![Website](https://img.shields.io/website-up-down-green-red/http/officefloor.net.svg?label=http://officefloor.net)](http://officefloor.net)

 ![Continuous Integration](https://github.com/officefloor/OfficeFloor/workflows/Continuous%20Integration/badge.svg)
 [![codecov](https://codecov.io/gh/officefloor/OfficeFloor/branch/master/graph/badge.svg)](https://codecov.io/gh/officefloor/OfficeFloor)

 [![Maven Central](https://img.shields.io/maven-central/v/net.officefloor/officefloor.svg)](https://search.maven.org/search?q=a:officefloor)
 ![GitHub](https://img.shields.io/github/license/officefloor/OfficeFloor)

# OfficeFloor

**Let AI add feature after feature, and your code stays tidy. Explicit Function Injection for Spring Boot REST.**

OfficeFloor is a Spring Boot add-on. Here is the problem it solves. Let an AI keep adding features to a normal codebase and it slowly erodes into a tangle. OfficeFloor stops that. Each endpoint is one YAML file that lists its steps in order. Each step is one small Java class. Adding a feature means adding a step. The AI gets one obvious place to put new code, and the rest stays put. Your Spring beans, security, and persistence carry on exactly as before.

More information and tutorials at [http://officefloor.net](http://officefloor.net)


## How AI erodes a Spring controller

A Spring `@RestController` that handles validation, business logic, and auditing in one class works fine at first. Then you ask an AI to add a feature. Then another. Each one piles more code into the same method. The method grows. The logic tangles. Bit by bit the code erodes. Soon the AI has to understand the whole tangle just to change one small thing. That is when it starts breaking what already worked.

OfficeFloor introduces an explicit YAML file per endpoint that declares the function steps, their order, and how outputs connect, while each function class continues to use Spring beans via normal injection.


## Explicit Function Injection your AI cannot erode

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

Each function class does one job. It declares only the Spring beans it needs. Spring injects them just as it would for any other bean. No function knows about the others. The YAML file lists every step, the order they run in, and where each one branches. That is the whole endpoint, in one file.

This is what keeps your app tidy as it grows. To add a feature you add a step. That means one small new class and one new line in the file. Everything already there stays exactly as it was. There is only one place each piece of logic can go, so an AI cannot scatter it around. The hundredth feature goes in the same clean way as the first. An AI still writes and changes these endpoints for you. It just cannot turn them into a tangle.

Because the whole structure sits in the files, OfficeFloor needs no extra AI tooling. There is no Model Context Protocol (MCP) server or plugin to work out how your endpoints connect. The YAML already says so. Frameworks that hide the flow bolt on such tooling to recover it. OfficeFloor does not need to.


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

Add only the starter matching your Spring Boot generation. Mixing versions causes runtime binary incompatibilities.

Spring's dependency injection, security, persistence, and actuator configuration remain completely intact. OfficeFloor enriches Spring, it does not replace it. You can start declaring endpoints as YAML files alongside your existing `@RestController` classes and migrate incrementally.


## Inversion of Coupling Control

The underlying paradigm behind OfficeFloor separates three concerns that most frameworks conflate:

* **Continuation Injection**: injecting functions to orchestrate application behaviour. This is what the YAML files express. It is the part Spring does not provide.
* **Dependency Injection**: injecting objects for state into functions. Spring already provides this.
* **Thread Injection**: injecting the thread (pool) to execute a particular function. Java 21 and 25 virtual threads now cover the common case, so this is mostly a tuning option.

Explicit YAML orchestration is the practical expression of Continuation Injection applied to REST endpoints. Read more in the paper [OfficeFloor: using office patterns to improve software design](http://doi.acm.org/10.1145/2739011.2739013) or the [introductory blog post](https://sagenschneider.blogspot.com/2019/02/inversion-of-coupling-control.html).


## Documentation

- [Getting started](docs/getting-started.md): one dependency and one YAML file, from zero to a running endpoint.
- [Spring Boot plugin overview](docs/spring-boot-plugin.md): what the plugin adds to Spring, progressive adoption, and the version-specific starters.
- [`@RestController` vs OfficeFloor YAML](docs/comparison.md): the same endpoint written both ways, how the directory layout indexes URL to code, and how the YAML makes an endpoint's flow explicit.
- [YAML endpoint configuration](docs/yaml-endpoint-configuration.md): full reference for the endpoint file (naming, steps, `next:`/`outputs:`, escalations, governance).
- [Spring integration](docs/spring-integration.md): how handler classes use Spring beans, MVC annotations, security, persistence, and actuator.
- [Tutorials](docs/tutorials.md): categorised, runnable examples. The full narrated series is at [officefloor.net/tutorials](http://officefloor.net/tutorials/index.html).
