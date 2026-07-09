# Spring Boot Plugin Tutorials

The OfficeFloor Spring Boot tutorial series covers the YAML endpoint model and its integration
with the wider Spring ecosystem. Each tutorial is a complete, runnable Spring Boot project.

The full series with narrated explanations is on the website:
[https://officefloor.net/tutorials/index.html](https://officefloor.net/tutorials/index.html).
Each entry below links to its runnable source on GitHub.

## Start here

* **[Getting Started](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestGettingStartedHttpServer)** — from zero to a running REST endpoint: one dependency, one YAML file. See also [Getting Started](getting-started.md).
* **[Spring REST HTTP Server](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestHttpServer)** — the YAML endpoint model in depth: naming conventions, multi-method service classes, multiple path variables, custom response headers.
* **[Spring Boot 3 REST](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestSpringBoot3HttpServer)** — choosing the correct version-specific starter for your Spring Boot generation.
* **[Spring REST to OfficeFloor Conversion Reference](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestConversionReference)** — the mechanical substitutions to convert a Spring MVC `@RestController` into YAML composition.

## Function orchestration

Composing an endpoint from small, single-purpose functions wired together in YAML — the function is the unit of composition, the YAML is the specification.

* **[Function](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestFunctionHttpServer)** — multi-function endpoints with `next:` and `outputs:` (Continuation Injection).
* **[Variable](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestVariableHttpServer)** — passing state downstream between functions via `Out<T>` / `@Val`, without coupling caller to callee.
* **[Governance](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestGovernanceHttpServer)** — wrapping function execution with cross-cutting concerns via `govern:`.
* **[Exception](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestExceptionHttpServer)** — handling exceptions (escalations) via `escalations:` and global handlers.
* **[REST CRUD Orchestration](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestCrudHttpServer)** — a full CRUD resource built from `Load`/`Build` producers, `Apply`/`Save` actions and `RespondWith` responders for GET, POST, PUT and DELETE.
* **[Related Entities](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestRelationshipHttpServer)** — two related entities showing a shared `Load` reused across endpoints and an ownership-scoped load that finds a child within its parent.
* **[Orchestration Patterns and Naming](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestOrchestrationReference)** — the function naming conventions and the DTO to entity to DTO data-flow, plus the one-shot request body, `@Valid` ordering and variable reference-semantics rules.

## Beyond Spring MVC — additional capabilities

Capabilities OfficeFloor provides over and above Spring MVC, through its function and thread injection.

* **[Managed Object](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestManagedObjectHttpServer)** — OfficeFloor Managed Objects, the native unit of state.
* **[Supplier](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestSupplierHttpServer)** — supplying a library of related managed objects from a single declaration.
* **[Team](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestTeamHttpServer)** — Thread Injection: assigning threads/pools to functions.

## Spring ecosystem integration

* **[Data JPA](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestDataJpaHttpServer)** — Spring Data JPA with YAML composition and transaction governance.
* **[Security](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestSecurityHttpServer)** — securing endpoints with Spring Security.
* **[Validation](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestValidationHttpServer)** — Bean Validation with YAML composition.
* **[Qualifier](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestQualifierHttpServer)** — `@Qualifier` injection into service methods.
* **[Actuator](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestActuatorHttpServer)** — Spring Boot Actuator production endpoints.
* **[OpenAPI](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestOpenApiHttpServer)** — YAML endpoints appearing in generated OpenAPI documentation.
* **[CORS](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestCorsHttpServer)** — configuring Cross-Origin Resource Sharing.
* **[Servlet](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestServletHttpServer)** — direct injection of `jakarta.servlet.http.HttpServletRequest`.
* **[Thymeleaf](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestThymeleafHttpServer)** — server-side HTML rendering from service methods.

## Other JVM languages and effect systems

* **[Kotlin](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestKotlinHttpServer)** — endpoint logic in Kotlin.
* **[Scala](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestScalaHttpServer)** — endpoint logic in Scala.
* **[JavaScript](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestJavaScriptHttpServer)** — JavaScript via GraalVM.
* **[Cats Effect](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestCatsHttpServer)** — using Cats Effect within a handler.
* **[ZIO](https://github.com/officefloor/OfficeFloor/tree/master/tutorials/springboot/SpringRestZioHttpServer)** — using ZIO within a handler.
