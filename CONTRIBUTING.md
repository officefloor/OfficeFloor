# Contributing to OfficeFloor

Thanks for your interest in OfficeFloor! Contributions of all kinds are welcome —
bug reports, documentation fixes, new tutorials, and code.

OfficeFloor is a Spring Boot add-on that adds explicit YAML orchestration to Spring Boot
REST. If you are new to the project, the [README](README.md) and the
[tutorial series](https://officefloor.net/tutorials/index.html) are the best starting points.

## Ways to contribute

- **Report a bug** — open a [bug report](https://github.com/officefloor/OfficeFloor/issues/new/choose).
  Please include the OfficeFloor version, your Spring Boot generation (3.x or 4.x) and which
  starter you use, your JDK version, and — where relevant — the YAML endpoint file that
  reproduces the problem.
- **Suggest a feature** — open a [feature request](https://github.com/officefloor/OfficeFloor/issues/new/choose)
  describing the problem you want to solve, not only the solution you have in mind.
- **Ask a question / discuss an idea** — use
  [GitHub Discussions](https://github.com/officefloor/OfficeFloor/discussions) rather than the
  issue tracker.
- **Improve the docs** — the docs under [`docs/`](docs/) and the tutorial projects under
  [`tutorials/`](tutorials/) are all fair game.

## Prerequisites

| Tool | Version |
| --- | --- |
| JDK | 17 (Temurin is used in CI) |
| Maven | 3.9.x (no wrapper is committed — install Maven yourself) |
| Git | any recent version |

The lowest supported Java version is 17 — code must compile and run on JDK 17.

If your shell does not already export `JAVA_HOME`, you can source the helper script:

```bash
source set_java_home.sh
```

## Building

The reactor build is driven from [`bom/pom.xml`](bom/pom.xml), **not** the root `pom.xml`.
To build and run the full test suite the way CI does (skipping the long-running stress tests):

```bash
mvn -V -B -e -DskipStress clean install --file bom/pom.xml
```

To build a single module while iterating, run Maven from that module's directory, e.g. the
Spring Boot plugin:

```bash
cd springboot
mvn clean install -DskipStress
```

Some integration tests are disabled by default and are only enabled in CI via environment
variables (Docker, AWS, GCloud, code coverage). You do not need Docker, AWS or a cloud account
for a normal build — those tests are skipped unless the corresponding
`OFFICEFLOOR_*_AVAILABLE` variable is set. See [`.github/workflows/ci.yml`](.github/workflows/ci.yml)
for the exact matrix.

## Making changes

1. **Fork** the repository and create a branch off `master`.
2. Keep changes focused — one logical change per pull request.
3. **Match the surrounding code style.** Follow the conventions already present in the file you
   are editing (indentation, naming, Javadoc). OfficeFloor source is tab-indented.
4. **Add or update tests.** Bug fixes should come with a test that fails before the fix; new
   features should be covered by tests. Many features are demonstrated by a runnable tutorial
   project under `tutorials/springboot/` — a new endpoint capability is often best shown by a
   new or extended tutorial.
5. **Update documentation** when behaviour or public API changes — including the relevant files
   under `docs/` and, for the Spring Boot plugin, the `context7.json` rules if the endpoint
   model changes.
6. Run the build locally (command above) before opening the pull request.

## Submitting a pull request

- Open the PR against the `master` branch and fill in the pull request template.
- Describe **what** changed and **why**. Link any related issue with `Fixes #123`.
- Ensure CI (the Continuous Integration workflow) is green. PRs are built on Linux, Windows and
  macOS with JDK 17.
- Be responsive to review feedback — maintainers may ask for changes before merging.

## License

OfficeFloor is licensed under the [Apache License 2.0](LICENSE.txt). By contributing, you agree
that your contributions will be licensed under the same license.

## Reporting security issues

Please do **not** report security vulnerabilities through public issues. See
[SECURITY.md](.github/SECURITY.md) (or the `/.well-known/security.txt` on
[officefloor.net](https://officefloor.net/.well-known/security.txt)) for how to report them
privately.
