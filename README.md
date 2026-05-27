# kube-diag-ai

`kube-diag-ai` is a work-in-progress Kubernetes diagnostic assistant focused on real cluster troubleshooting data. It starts with Pod diagnostics and is designed as a practical foundation for exploring AI-assisted, tool-based cloud-native operations without pretending that the AI layer is complete.

The project is built with Spring Boot 4 and Java 21, with clean architecture boundaries around Kubernetes access, future AI analysis, and REST input.

## Why This Project

The goal is not to build a generic chatbot. The goal is to explore how an AI-assisted diagnostic workflow can be grounded in real Kubernetes signals and clean software boundaries.

This project is intended to exercise:

- Kubernetes diagnostics using read-only cluster data
- Clean architecture boundaries between domain, application, and adapters
- Tool-based AI and future agentic workflows
- Replaceable integrations for Kubernetes clients and LLM providers
- Local reproducibility with a disposable k3d cluster
- Future GitOps-based deployment into a real platform

## Current Status

### Implemented

- Spring Boot 4 / Java 21 application
- Lightweight hexagonal architecture
- REST endpoint: `POST /api/pods/diagnose`
- Fabric8 Kubernetes adapter behind `KubernetesDiagnosticsPort`
- Pod diagnostics using:
  - pod phase
  - container states
  - restart counts
  - waiting reasons
  - recent pod logs
  - Kubernetes events
- Local k3d development environment
- Demo pods:
  - `pod-ok`
  - `pod-crashloop`
  - `pod-imagepullbackoff`
- Makefile workflow:
  - `dev-up`
  - `run`
  - `smoke-test`
  - `dev-down`

### In Progress

- Improving the Fabric8 diagnostics adapter structure and test coverage
- Expanding Pod diagnostic signals into more useful structured findings
- Keeping the application ready for future AI analysis without coupling the domain to an AI framework

### Planned

- Docker packaging
- In-cluster deployment with `ServiceAccount` and read-only RBAC
- LangChain4j-based AI analysis
- Structured AI diagnosis reports
- OpenTelemetry observability
- GitOps deployment through Argo CD
- Support for more Kubernetes resources such as Services, Ingresses, and Certificates

## Architecture

Current diagnostic flow:

```text
User
  -> REST API
  -> DiagnosePodUseCase
  -> KubernetesDiagnosticsPort
  -> Fabric8 Kubernetes adapter
  -> Kubernetes API
```

Future AI analysis flow:

```text
DiagnosePodUseCase
  -> AiAnalysisPort
  -> LangChain4j / LLM provider
```

## Hexagonal Architecture

The project follows a lightweight hexagonal architecture:

- `domain` is framework-free and contains diagnostic request/result models.
- `application` depends on inbound and outbound ports, not concrete infrastructure.
- `adapter.in.rest` isolates Spring MVC request handling.
- `adapter.out.kubernetes` isolates Fabric8 and Kubernetes API details.
- `adapter.out.ai` is currently a stub and will later isolate LangChain4j or another LLM provider.

Kubernetes and AI providers are intentionally replaceable. The domain and application layers do not depend on Fabric8, Spring, LangChain4j, or Kubernetes model types.

## Current Features

The current API can diagnose a Pod by namespace and name.

For a requested Pod, the application can:

- retrieve pod status
- analyze container states
- detect restart counts
- detect waiting reasons such as `CrashLoopBackOff` and `ImagePullBackOff`
- collect recent pod logs
- collect Kubernetes events related to the Pod
- return structured findings with severity, message, and details

AI analysis is not implemented yet. The current AI adapter returns placeholder data so the application shape can evolve without coupling the core workflow to a provider too early.

## Local Development

The local development workflow runs the Spring Boot application on your machine and connects to a disposable k3d Kubernetes cluster through your local kubeconfig.

### Prerequisites

- Java 21
- Docker
- kubectl
- k3d
- make

### Start the local cluster

```bash
make dev-up
```

This creates or reuses a k3d cluster named `kube-diag-dev`, switches kubectl to `k3d-kube-diag-dev`, creates the `demo` namespace, applies the demo Pod fixtures, and waits for them to reach meaningful diagnostic states.

Expected demo workloads:

- `demo/pod-ok`
- `demo/pod-crashloop`
- `demo/pod-imagepullbackoff`

You can inspect them with:

```bash
make kube-status
```

You can also run the wait step directly:

```bash
make wait-fixtures
```

This waits for `pod-ok` to become Running or Ready, `pod-crashloop` to expose `CrashLoopBackOff` or a restart count, and `pod-imagepullbackoff` to expose `ImagePullBackOff` or `ErrImagePull`.

### Run the application locally

```bash
make run
```

The application runs on the developer machine and uses kubeconfig to talk to the k3d Kubernetes API. It is not deployed inside the cluster.

### Run smoke tests

In another terminal:

```bash
make smoke-test
```

The smoke test first runs `make wait-fixtures`, then calls `POST /api/pods/diagnose` for the three demo Pods. It should return findings that include pod status, logs, and events when available.

### Clean up

```bash
make dev-down
```

This deletes the local k3d cluster.

## Example Request

```bash
curl -X POST http://localhost:8080/api/pods/diagnose \
  -H 'Content-Type: application/json' \
  -d '{
    "namespace": "demo",
    "podName": "pod-crashloop"
  }'
```

## Target Deployment Architecture

The planned deployment path is:

1. Build a Docker image for `kube-diag-ai`.
2. Publish the image to GHCR.
3. Generate hydrated Kubernetes manifests.
4. Store those manifests in the platform GitOps repository.
5. Deploy through Argo CD on the Rancher/RKE2 platform from `passga/terraform-aws-platform`.

This deployment path is not implemented yet. The application currently runs locally and talks to k3d through kubeconfig.

## Roadmap

- Improve and harden Fabric8 Pod diagnostics
- Add Docker packaging
- Add in-cluster deployment with `ServiceAccount` and read-only RBAC
- Add LangChain4j AI analysis
- Add structured AI diagnosis reports
- Add OpenTelemetry observability
- Add GitOps deployment through Argo CD
- Support more Kubernetes resources:
  - Services
  - Ingresses
  - Certificates

## Security Principles

- Kubernetes access should be read-only.
- No automatic remediation at first.
- No destructive actions.
- Explicit permissions through RBAC when in-cluster deployment is added.
- Human-in-the-loop diagnostics before any operational action.

## Related Project

- `passga/terraform-aws-platform`: target platform repository for the future Rancher/RKE2/Argo CD integration.
