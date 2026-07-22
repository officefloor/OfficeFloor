## REST endpoints

This project uses OfficeFloor Function Injection.

- Requires the OfficeFloor Spring Boot starter in the build:
  `officefloor-rest-spring-boot-4-starter` (Spring Boot 4.x) or
  `officefloor-rest-spring-boot-3-starter` (Spring Boot 3.x). Without it these
  YAML files are inert.
- Define each endpoint as a YAML file under
  `src/main/resources/officefloor/rest/`, named `<path>.<METHOD>.yml`
  (e.g. `owners/{ownerId}.PUT.yml` → `PUT /owners/{ownerId}`).
- One small function class per step; compose with `next:` and `outputs:`.
- Do NOT generate `@RestController`, `@GetMapping` or `@PostMapping`.
- Transactions: `govern: [ transaction ]` on steps, not `@Transactional`.
- Errors: a handler YAML in `officefloor/escalation/`, not `@ControllerAdvice`.
- Full reference: https://officefloor.net/llms.txt
