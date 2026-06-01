# AGENTS.md

# kube-diag-ai Agent Guidelines

This document defines the engineering rules that contributors and coding agents must follow.

## Project Goal

kube-diag-ai is a Kubernetes diagnostic assistant.

The objective is to collect Kubernetes runtime signals and provide structured diagnostics while keeping a clear separation between business logic and infrastructure concerns.

The project prioritizes:

- maintainability
- testability
- readability
- incremental delivery
- production-oriented design

---

## Architecture

This project follows a Hexagonal Architecture (Ports & Adapters).

### Rules

- Application code must not depend on Kubernetes client implementations.
- Application code must not depend on Fabric8 classes.
- Application code must not depend on Spring annotations.
- Domain and application layers must remain framework-agnostic.
- Controllers belong to `adapter.in`.
- Infrastructure implementations belong to `adapter.out`.
- Spring configuration belongs to `adapter.config`.
- Dependencies must always point toward the application core.

### Adapters

External technologies must remain isolated behind ports.

Examples:

- Kubernetes API
- AI providers
- Databases
- Messaging systems

must never leak into the application layer.

---

## Testing

Every feature must include tests.

Minimum expectations:

- unit tests for business logic
- mapping tests when relevant
- controller tests when relevant
- existing tests must continue to pass

Before submitting changes:

```bash
./mvnw test
```

must pass.

---

## Smoke Tests

Smoke tests must validate business behavior.

Avoid tests that only verify:

- application startup
- endpoint availability
- HTTP 200 responses

Prefer assertions that validate expected diagnostic results.

---

## Scope Control

Do not introduce new technologies unless explicitly requested by the issue.

Examples:

- LangChain4j
- OpenAI integrations
- Prometheus
- Loki
- Helm
- Argo CD
- Docker image publishing
- GitOps deployment

must not be added outside the scope of the current issue.

---

## Pull Requests

Keep pull requests focused.

Rules:

- one issue = one PR
- avoid unrelated refactoring
- avoid drive-by fixes
- keep diffs small when possible

A PR should solve the issue and nothing more.

---

## Git Branch Naming

Use the following conventions:

```text
feature/<short-description>
fix/<short-description>
refactor/<short-description>
docs/<short-description>
ci/<short-description>
test/<short-description>
```

Examples:

```text
feature/namespace-pod-summary
feature/namespace-discovery
fix/multi-container-log-collection
ci/github-actions-workflow
test/improve-smoke-tests
```

Do not use:

```text
codex
ai
assistant
generated
```

inside branch names.

---

## Commit Messages

Follow Conventional Commits.

Examples:

```text
feat: add namespace pod summary endpoint
fix: collect logs from all pod containers
test: improve smoke test assertions
docs: document hexagonal architecture
ci: add GitHub Actions workflow
refactor: extract pod health evaluator
```

Avoid generic commit messages such as:

```text
update code
fix issue
improvements
changes
misc updates
```

---

## API Design

Use REST conventions.

Examples:

```text
GET    -> retrieve resources
POST   -> execute commands or diagnostics
PUT    -> replace resources
PATCH  -> partial updates
DELETE -> remove resources
```

Prefer resource discovery workflows:

```text
Namespaces
  -> Pods
    -> Diagnostics
```

instead of requiring users to know internal Kubernetes resource names.

---

## AI Features

AI is not the primary goal of this project.

AI integrations must:

- remain behind dedicated ports
- be replaceable
- be observable
- be testable
- never directly modify cluster resources

Diagnostics must remain evidence-based and explainable.

---

## Decision Making

When several implementations are possible:

1. Prefer the simplest solution.
2. Prefer maintainability over cleverness.
3. Prefer explicit code over magic.
4. Prefer small incremental changes.
5. Preserve architectural boundaries.

## Dependency Management

Do not introduce new dependencies unless they are required to solve the current issue.

Prefer existing project dependencies when possible.

New dependencies must provide clear value and remain aligned with the project roadmap.

## Testing Conventions

Test method names should describe business behavior.

Preferred format:

should_<expected_behavior>_when_<condition>

Examples:

should_return_healthy_when_pod_is_running_and_ready
should_return_unhealthy_when_waiting_reason_is_crashloopbackoff
should_return_warning_when_pod_is_terminating

Avoid names that only describe implementation details.
