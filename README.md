# kube-diag-ai

> Work in progress: this project is an early skeleton. The REST API and hexagonal architecture boundaries are being shaped, while AI integration is currently stubbed.

Spring Boot 4 and Java 21 service skeleton for an AI Kubernetes diagnostic assistant.

## Architecture

- `domain`: pod diagnostic command, result models, and finding severity.
- `application`: use case and outbound ports.
- `adapter.in.rest`: REST controller for pod diagnostic requests.
- `adapter.out.kubernetes`: Fabric8-based Kubernetes diagnostics adapter.
- `adapter.out.ai`: placeholder AI analysis adapter.

The Kubernetes adapter uses the local kubeconfig and stays behind the application port. The AI adapter intentionally returns stub data and can be replaced later without changing the use case or REST API.

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

## Local Development

Required tools:

- Java 21
- Docker
- kubectl
- k3d
- make

Start a disposable local Kubernetes cluster and install the demo pod fixtures:

```bash
make dev-up
```

This creates a k3d cluster named `kube-diag-dev`, switches kubectl to `k3d-kube-diag-dev`, creates the `demo` namespace, and applies three pod fixtures:

- `pod-ok`
- `pod-crashloop`
- `pod-imagepullbackoff`

Run the Spring Boot app locally on your machine:

```bash
make run
```

In another terminal, call the diagnostic endpoint for all demo pods:

```bash
make smoke-test
```

You can also call one pod directly:

```bash
curl -X POST http://localhost:8080/api/pods/diagnose \
  -H 'Content-Type: application/json' \
  -d '{"namespace":"demo","podName":"pod-crashloop"}'
```

Check the Kubernetes fixture state:

```bash
make kube-status
```

Remove the local cluster:

```bash
make dev-down
```
