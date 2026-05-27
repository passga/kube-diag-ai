# kube-diag-ai

> Work in progress: this project is an early skeleton. The REST API and hexagonal architecture boundaries are being shaped, while Kubernetes and AI integrations are currently stubbed.

Spring Boot 4 and Java 21 service skeleton for an AI Kubernetes diagnostic assistant.

## Architecture

- `domain`: pod diagnostic command, result models, and finding severity.
- `application`: use case and outbound ports.
- `adapter.in.rest`: REST controller for pod diagnostic requests.
- `adapter.out.kubernetes`: placeholder Kubernetes diagnostics adapter.
- `adapter.out.ai`: placeholder AI analysis adapter.

The outbound adapters intentionally return stub data. They can be replaced later with a Kubernetes client implementation and an LLM provider implementation without changing the use case or REST API.

## Run

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test
```

## Example Request

```bash
curl -X POST http://localhost:8080/api/pods/diagnose \
  -H 'Content-Type: application/json' \
  -d '{
    "namespace": "argocd",
    "podName": "argocd-server-xxx"
  }'
```
