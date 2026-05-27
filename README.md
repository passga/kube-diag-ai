# kube-diag-ai

`kube-diag-ai` is a work-in-progress Kubernetes diagnostic assistant.

The project starts with a simple but real use case: diagnosing Kubernetes Pods from live cluster data such as status, container states, restart counts, waiting reasons, logs, and events.

The long-term goal is to evolve this project into an AI-assisted Kubernetes diagnostic assistant with production-minded guardrails: read-only access, explicit RBAC, evidence-based explanations, and human validation.

This is not intended to be a generic chatbot or an autonomous remediation bot.

---

## Why This Project

Modern Kubernetes platforms produce a lot of diagnostic signals:

- Pod status
- container states
- restart counts
- waiting reasons
- Kubernetes events
- logs
- Services
- Ingresses
- Certificates
- metrics
- traces

The problem is not only accessing this data.  
The real challenge is helping a developer or platform engineer understand what is probably wrong, why it is wrong, and what to check next.

`kube-diag-ai` is designed as a practical learning project around:

- Kubernetes diagnostics
- clean backend architecture
- hexagonal architecture
- read-only infrastructure access
- future AI-assisted analysis
- tool-based AI workflows
- GitOps-based deployment
- platform engineering practices

The goal is to build something more credible than a chatbot: a diagnostic assistant grounded in real Kubernetes evidence.

---

## Project Vision

The target vision is an AI Ops Assistant for Kubernetes platforms.

The assistant should eventually help answer questions such as:

- Why is my Pod in `CrashLoopBackOff`?
- Why is my Pod stuck in `ImagePullBackOff`?
- Why is my application not reachable through an Ingress?
- Why does my Service of type `LoadBalancer` not expose an external address?
- Why is my cert-manager `Certificate` not `Ready`?
- Why is an Argo CD application not healthy?
- Why is a Rancher-managed workload failing?

The assistant should collect read-only signals from Kubernetes and observability tools, then produce a structured diagnostic report.

The report should include:

- probable cause
- supporting evidence
- impacted Kubernetes resources
- useful diagnostic commands
- confidence level
- possible next checks
- no automatic remediation by default

The AI layer should assist the operator, not replace the operator.

---

## Current Status

### Implemented

The current implementation provides a first technical foundation:

- Spring Boot 4 / Java 21 application
- Maven wrapper
- lightweight hexagonal architecture
- REST endpoint: `POST /api/pods/diagnose`
- Fabric8 Kubernetes client integration
- read-only Kubernetes diagnostics through a Kubernetes outbound port
- Pod diagnostics using:
  - Pod phase
  - Pod conditions
  - container states
  - init container states
  - restart counts
  - waiting reasons
  - recent logs
  - Kubernetes events
- non-fatal findings when logs or events cannot be retrieved
- local k3d development environment
- demo Pods:
  - `pod-ok`
  - `pod-crashloop`
  - `pod-imagepullbackoff`
- OpenAPI / Swagger UI documentation
- Makefile workflow:
  - `dev-up`
  - `run`
  - `smoke-test`
  - `wait-fixtures`
  - `kube-status`
  - `dev-down`
  - `dev-reset`
- README documentation for local development

### Not Implemented Yet

The following parts are intentionally not implemented yet:

- AI / LLM analysis
- LangChain4j integration
- Docker image packaging
- GHCR publishing
- in-cluster deployment
- Kubernetes `ServiceAccount`
- read-only RBAC manifests
- Helm chart
- Argo CD deployment
- GitOps manifests
- OpenTelemetry instrumentation
- Prometheus / Grafana dashboards
- Loki integration
- diagnosis of Services, Ingresses, Certificates, or Argo CD applications

---

## MVP Scope

The current MVP focuses on diagnosing a known Pod.

Current endpoint:

```http
POST /api/pods/diagnose
```

Example request:

```json
{
  "namespace": "demo",
  "podName": "pod-crashloop"
}
```

This is a useful low-level diagnostic primitive, but it requires the caller to know the exact Pod name.

That is not ideal in real Kubernetes environments because Pod names are often generated and unstable, especially when managed by Deployments or ReplicaSets.

Examples:

```text
argocd-server-5f7c9d8f9b-xk2pz
my-app-78d9cbb6c9-vr42m
```

A more useful product workflow will be added later:

1. List Pods in a namespace with their health status.
2. Select a problematic Pod.
3. Run a detailed diagnostic on that Pod.
4. Later, diagnose a workload directly by Deployment, StatefulSet, or DaemonSet name.

Example future endpoint:

```http
GET /api/namespaces/{namespace}/pods
```

Example future workflow:

```text
GET /api/namespaces/demo/pods
  -> find unhealthy Pods

POST /api/pods/diagnose
  -> diagnose selected Pod
```

---

## Target Use Case

The long-term target is a service deployed inside a Kubernetes platform.

Target runtime model:

```text
User
  -> kube-diag-ai API
  -> Kubernetes API read-only
  -> Observability backends
  -> LLM provider
```

Target Kubernetes deployment model:

```text
Namespace: ai-ops
ServiceAccount: ai-ops-reader
RBAC: read-only
Deployment: kube-diag-ai
Ingress: aiops.<domain>
```

The assistant should be able to run inside a cluster with limited permissions and query only the resources it needs.

The intended deployment path is:

1. Build a Docker image.
2. Publish the image to GHCR.
3. Generate Kubernetes manifests.
4. Store the manifests in a GitOps repository.
5. Deploy the application through Argo CD.
6. Run it with a read-only Kubernetes `ServiceAccount`.

This target deployment is not implemented yet.

---

## Relationship With Platform Repository

This project is intended to integrate later with the platform repository:

```text
passga/terraform-aws-platform
```

Target platform:

```text
AWS
  -> Rancher management cluster
  -> RKE2 downstream cluster
  -> Argo CD
  -> kube-diag-ai
```

The platform repository provides the infrastructure foundation.  
`kube-diag-ai` is intended to become a workload deployed on top of that platform.

---

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

Future AI-assisted flow:

```text
User
  -> REST API
  -> DiagnosePodUseCase
  -> KubernetesDiagnosticsPort
  -> Kubernetes API
  -> AiAnalysisPort
  -> LangChain4j / LLM provider
```

The current implementation does not call an LLM yet.

---

## Hexagonal Architecture

The project follows a lightweight hexagonal architecture.

```text
domain
  -> framework-free diagnostic models

application
  -> use cases
  -> inbound ports
  -> outbound ports

adapter.in.rest
  -> Spring MVC REST controller

adapter.out.kubernetes
  -> Fabric8 Kubernetes client
  -> Kubernetes API access
  -> Kubernetes mappers
  -> collectors
  -> severity classifier

adapter.out.ai
  -> placeholder for future AI provider integration
```

Architecture rules:

- The domain layer must remain framework-free.
- The application layer must depend on ports, not concrete infrastructure.
- Spring MVC must stay in the REST adapter.
- Fabric8 types must stay inside the Kubernetes adapter.
- Kubernetes API model types must not leak into domain or application layers.
- AI framework types must not leak into domain or application layers.
- Kubernetes access should stay replaceable.
- LLM provider integration should stay replaceable.

---

## Current Package Structure

Main package:

```text
com.example.kubediagai
```

Important areas:

```text
domain
application
adapter.in.rest
adapter.out.kubernetes
adapter.out.ai
adapter.config
```

Kubernetes adapter responsibilities are split into smaller components:

```text
adapter.out.kubernetes
  Fabric8KubernetesDiagnosticsAdapter
  KubernetesAdapterConfiguration

adapter.out.kubernetes.collector
  Fabric8PodLogCollector
  Fabric8PodEventCollector

adapter.out.kubernetes.mapper
  Fabric8PodToClusterFindingMapper
  ContainerStatusFindingMapper
  PodConditionFindingMapper
  PodEventFindingMapper
  PodLogFindingMapper

adapter.out.kubernetes.classifier
  KubernetesSeverityClassifier
```

The top-level adapter orchestrates Kubernetes diagnostics.  
Collectors retrieve logs and events.  
Mappers transform Kubernetes data into domain findings.  
The classifier centralizes severity rules.

---

## Current Features

For a requested Pod, the application can currently:

- retrieve the Pod from Kubernetes
- detect when the Pod does not exist
- read the Pod phase
- read Pod conditions
- inspect regular containers
- inspect init containers
- detect waiting reasons
- detect restart counts
- detect failing states such as:
  - `CrashLoopBackOff`
  - `ImagePullBackOff`
  - `ErrImagePull`
- collect recent logs per container
- collect Kubernetes events related to the Pod
- return structured findings

A finding contains:

- severity
- message
- details

Example severity levels:

```text
INFO
WARNING
CRITICAL
```

---

## Safety Principles

This project is designed around safe diagnostic behavior.

Principles:

- read-only Kubernetes access
- no automatic remediation in the first versions
- no destructive actions
- explicit RBAC permissions when deployed in-cluster
- human-in-the-loop diagnostics
- evidence-based explanations
- clear separation between observation and action
- observable application behavior through logs, metrics, and traces later

The assistant should explain and suggest, not silently change infrastructure.

---

## Local Development

The local development workflow runs the Spring Boot application on your machine and connects to a disposable k3d Kubernetes cluster through your local kubeconfig.

The application is not deployed inside the local k3d cluster in the current version.

### Prerequisites

Required tools:

- Java 21
- Docker
- kubectl
- k3d
- make

On Windows, using WSL2 is recommended.

---

## Start the Local k3d Cluster

Run:

```bash
make dev-up
```

This command:

1. creates or reuses a k3d cluster named `kube-diag-dev`
2. switches kubectl to the `k3d-kube-diag-dev` context
3. creates the `demo` namespace
4. deploys demo Pod fixtures
5. waits for the fixtures to reach meaningful diagnostic states
6. prints the Pod status

Demo Pods:

```text
demo/pod-ok
demo/pod-crashloop
demo/pod-imagepullbackoff
```

You can inspect them with:

```bash
make kube-status
```

You can run only the wait step with:

```bash
make wait-fixtures
```

---

## Run the Application

Run:

```bash
make run
```

This starts the Spring Boot application locally.

The application uses your kubeconfig to access the k3d Kubernetes API.

Default local URL:

```text
http://localhost:8080
```

---

## Run Tests

Run:

```bash
./mvnw test
```

---

## Run Smoke Tests

Start the application first:

```bash
make run
```

Then, from another terminal:

```bash
make smoke-test
```

The smoke test calls the diagnosis endpoint for:

```text
pod-ok
pod-crashloop
pod-imagepullbackoff
```

It should return findings including Pod status, logs, and events when available.

If your application runs on another port:

```bash
APP_URL=http://localhost:18080 make smoke-test
```

---

## Example Request

```bash
curl -X POST http://localhost:8080/api/pods/diagnose \
  -H 'Content-Type: application/json' \
  -d '{
    "namespace": "demo",
    "podName": "pod-crashloop"
  }'
```

## API Documentation

Start the application locally:

```bash
make run
```

Then open Swagger UI in a browser:

```text
http://localhost:8080/swagger-ui/index.html
```

The OpenAPI JSON document is available at:

```text
http://localhost:8080/v3/api-docs
```

---

## Example Response Shape

The exact findings depend on the current Kubernetes state.

Example shape:

```json
{
  "namespace": "demo",
  "podName": "pod-crashloop",
  "findings": [
    {
      "severity": "WARNING",
      "message": "Container is waiting",
      "details": "Container app is waiting with reason CrashLoopBackOff"
    },
    {
      "severity": "INFO",
      "message": "Recent logs for container app",
      "details": "..."
    },
    {
      "severity": "INFO",
      "message": "Recent events",
      "details": "..."
    }
  ]
}
```

---

## Clean Up

Delete the local k3d cluster:

```bash
make dev-down
```

Reset the local environment:

```bash
make dev-reset
```

---

## AI Analysis Roadmap

AI analysis is not implemented yet.

The future AI layer should take structured Kubernetes findings as input and produce a diagnostic explanation.

Future response could include:

```json
{
  "probableCause": "The application container is repeatedly crashing after startup.",
  "evidence": [
    "Container is waiting with reason CrashLoopBackOff",
    "Restart count is greater than zero",
    "Recent logs show application startup failure"
  ],
  "suggestedCommands": [
    "kubectl logs pod-crashloop -n demo",
    "kubectl describe pod pod-crashloop -n demo",
    "kubectl get events -n demo --sort-by=.lastTimestamp"
  ],
  "confidence": "MEDIUM"
}
```

The AI layer should be implemented behind an outbound port, for example:

```text
AiAnalysisPort
```

Potential provider:

```text
LangChain4j / LLM provider
```

The domain and application layers should not depend directly on the AI framework.

---

## Roadmap

### Short Term

- Add namespace Pod summary endpoint:
  - `GET /api/namespaces/{namespace}/pods`
- Improve README and API documentation
- Improve structured diagnostic findings
- Add more tests around Kubernetes mapping rules

### Medium Term

- Diagnose workloads by Deployment or StatefulSet name
- Add Docker packaging
- Publish image to GHCR
- Add Kubernetes manifests
- Add in-cluster deployment
- Add read-only `ServiceAccount`
- Add RBAC manifests
- Add basic OpenTelemetry instrumentation

### Longer Term

- Add LangChain4j AI analysis
- Add structured AI diagnostic reports
- Add support for Services
- Add support for Ingresses
- Add support for cert-manager Certificates
- Add Prometheus metrics integration
- Add Loki logs integration
- Add Grafana dashboard
- Deploy through Argo CD
- Integrate with the Rancher/RKE2 platform repository

---

## Out of Scope For Now

The following are intentionally out of scope for the first versions:

- automatic remediation
- writing to Kubernetes resources
- restarting Pods
- scaling Deployments
- modifying Services or Ingresses
- changing cert-manager resources
- executing arbitrary shell commands
- cluster-admin permissions
- autonomous production actions

---

## Development Principles

This repository is also a learning and demonstration project.

Important principles:

- prefer small, understandable components
- avoid leaking infrastructure details into the domain
- keep adapters replaceable
- keep diagnostics explainable
- keep AI usage grounded in collected evidence
- add tests for diagnostic rules
- document what is implemented and what is only planned
- avoid overselling incomplete AI features

---

## Related Project

Platform repository:

```text
passga/terraform-aws-platform
```

This repository is intended to provide the future deployment platform for `kube-diag-ai`:

```text
AWS -> Rancher -> RKE2 -> Argo CD -> kube-diag-ai
```
