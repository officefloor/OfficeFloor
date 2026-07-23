## OfficeFloor REST endpoints

Applies when adding or changing REST endpoints. This project serves REST through
OfficeFloor Function Injection, **not** Spring MVC controllers: each endpoint is a
pipeline of small function classes wired together in YAML.

- Requires the OfficeFloor Spring Boot starter in the build:
  `officefloor-rest-spring-boot-4-starter` (Spring Boot 4.x) or
  `officefloor-rest-spring-boot-3-starter` (Spring Boot 3.x). Without it these YAML
  files are inert. (Rules below track these starter versions.)
- Define each endpoint as a YAML file under `src/main/resources/officefloor/rest/`,
  named `<path>.<METHOD>.yml` (e.g. `owners/{ownerId}.PUT.yml` → `PUT /owners/{ownerId}`).
- One small function class per step; compose steps with `next:` and `outputs:`.
- Do NOT generate `@RestController`, `@GetMapping` or `@PostMapping`.
- Transactions: `govern: [ transaction ]` on steps, not `@Transactional`.
- Errors: a handler YAML in `officefloor/escalation/`, not `@ControllerAdvice`.

### Worked example — `PUT /owners/{ownerId}`

The whole shape of an endpoint. The sections after this one are the reference.

`src/main/resources/officefloor/rest/owners/{ownerId}.PUT.yml`

```yaml
# First step runs first. Validate before Load so an invalid body is a 400,
# not masked by a 404 for a missing owner.
validate:
  class: com.example.owner.ValidateOwner
  next: load
load:
  class: com.example.owner.LoadOwner
  next: apply
apply:
  class: com.example.owner.ApplyOwner
  govern: [ transaction ]
  next: save
save:
  class: com.example.owner.SaveOwner
  govern: [ transaction ]
  next: respond
respond:
  class: com.example.owner.RespondWithOwner
```

```java
// One public method each. State moves by Out<T> (set) → @Val (read), matched by type.
public class ValidateOwner {
  public void validate(@RequestBody @Valid OwnerRequest request, Out<OwnerRequest> body) {
    body.set(request); // body read once here; republished for later steps
  }
}
public class LoadOwner {
  public void load(@PathVariable("ownerId") int ownerId, OwnerRepository repository,
      Out<Owner> ownerOut) throws OwnerNotFoundException {
    Owner owner = repository.findById(ownerId);
    if (owner == null) throw new OwnerNotFoundException(ownerId); // handled below
    ownerOut.set(owner);
  }
}
public class ApplyOwner { // @Val yields the stored object, not a copy — mutate in place
  public void apply(@Val OwnerRequest request, @Val Owner owner) {
    owner.setFirstName(request.firstName());
    owner.setLastName(request.lastName());
  }
}
public class SaveOwner {
  public void save(@Val Owner owner, OwnerRepository repository) {
    repository.save(owner);
  }
}
public class RespondWithOwner {
  public void respond(@Val Owner owner, ObjectResponse<OwnerResponse> response) {
    response.send(OwnerResponse.from(owner)); // this step responds; 200 by default
  }
}
```

`src/main/resources/officefloor/escalation/com.example.owner.OwnerNotFoundException.yml`

```yaml
handle:
  class: com.example.owner.HandleOwnerNotFound
```

```java
public class HandleOwnerNotFound {
  public void handle(@Parameter OwnerNotFoundException ex,
      ObjectResponse<ResponseEntity<String>> response) {
    response.send(new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND));
  }
}
```

### Step wiring

Each top-level YAML entry is a developer-chosen step name; the first is the entry
point. `class:` names the function. **Give each function class exactly one public
method** — several public methods fail at start-up unless every reference adds
`method:`.

- `next: <step>` — run that step afterwards. This step's return value arrives there
  as `@Parameter T`.
- `outputs: { <name>: <step> }` — conditional branches. Declare a
  `@FunctionalInterface` parameter annotated `@Flow("<name>")` and call it to take the
  branch; not calling it short-circuits.

### Function parameters

Declare only what the step needs; they resolve by role:

- `@PathVariable`, `@RequestParam`, `@RequestBody` — Spring MVC annotations work.
- Spring beans (repositories, mappers, services) — injected by type as normal.
- `ObjectResponse<T>` (`net.officefloor.web.ObjectResponse`) — send the response with
  `response.send(dto)`. This is how a step responds; wrap as
  `ObjectResponse<ResponseEntity<T>>` to set status explicitly.
- `@Parameter T` (`net.officefloor.plugin.section.clazz.Parameter`) — the previous
  step's return value, a `@Flow` argument, or a thrown escalation.

### Passing state between steps — `Out<T>` / `@Val`

Steps do not call each other. Beyond the single `@Parameter` hand-off, publish state
into a variable (`net.officefloor.plugin.variable`):

- Producer declares `Out<T>` and calls `set(...)`; consumer declares `@Val T`.
- Matching is **by type** — two variables of the same type in one pipeline need a
  `@Qualifier` annotation to disambiguate.
- `@Val` yields the same object the producer stored, **not a copy** — so an `Apply`
  step mutates the entity in place and later steps see the change.

### Naming

Verb plus entity: `Load<E>` (fetch by path variable, publishes `Out<E>`, throws when
absent) · `Build<E>` (construct from body) · `Validate<E>` (bind and validate the body,
publish it) · `Apply<E>` (mutate) · `Save<E>` · `Delete<E>` · `RespondWith<E>` (200) ·
`RespondWith<E>Created` (201) · `RespondWithNoContent` (204).

Keep DTOs at the edges — request body in at the first step, response DTO out at the
responder. Steps in between work with entities.

### Request body and validation

- The HTTP body can be read only once: **only one step per pipeline may bind
  `@RequestBody`**. A second binding fails at runtime. When later steps need it, the
  first step publishes it as a variable.
- `@Valid` runs before that step's method body, so step order decides when validation
  happens. Put the validating step first — otherwise a missing id returns 404 before an
  invalid body can return 400.

### Transactions

`govern: [ transaction ]` for writes, `govern: [ readonly-transaction ]` for reads,
listed on **every** step it covers. Both are provided by the starter. Governance spans
the pipeline, so the request commits once at the end.

### Errors

Functions throw; handlers respond. The exception must be **checked** (`extends
Exception`) so it appears in the `throws` clause. Put the handler in
`officefloor/escalation/<fully.qualified.ExceptionClass>.yml`, taking the exception as
`@Parameter` and responding via `ObjectResponse`. Matching is most-specific-first;
anything unmatched falls through to Spring `@RestControllerAdvice`.

### Security

Guard a whole endpoint file with a Spring Security SpEL expression:

```yaml
composition:
  authorize: "hasRole('OWNER_ADMIN')"
```

- Full reference: https://officefloor.net/llms.txt
