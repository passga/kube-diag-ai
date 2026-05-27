K3D_CLUSTER ?= kube-diag-dev
KUBE_CONTEXT ?= k3d-$(K3D_CLUSTER)
DEMO_NAMESPACE ?= demo
APP_URL ?= http://localhost:8080

.PHONY: dev-up run smoke-test dev-down kube-status dev-reset

dev-up:
	k3d cluster list $(K3D_CLUSTER) >/dev/null 2>&1 || k3d cluster create --config local/k3d/cluster.yaml --wait
	kubectl config use-context $(KUBE_CONTEXT)
	kubectl apply -f local/fixtures/namespace.yaml
	kubectl apply -f local/fixtures/pod-ok.yaml
	kubectl apply -f local/fixtures/pod-crashloop.yaml
	kubectl apply -f local/fixtures/pod-imagepullbackoff.yaml
	kubectl get pods -n $(DEMO_NAMESPACE)

run:
	mvn spring-boot:run

smoke-test:
	curl -fsS -X POST $(APP_URL)/api/pods/diagnose -H 'Content-Type: application/json' -d '{"namespace":"$(DEMO_NAMESPACE)","podName":"pod-ok"}'
	curl -fsS -X POST $(APP_URL)/api/pods/diagnose -H 'Content-Type: application/json' -d '{"namespace":"$(DEMO_NAMESPACE)","podName":"pod-crashloop"}'
	curl -fsS -X POST $(APP_URL)/api/pods/diagnose -H 'Content-Type: application/json' -d '{"namespace":"$(DEMO_NAMESPACE)","podName":"pod-imagepullbackoff"}'

dev-down:
	k3d cluster delete $(K3D_CLUSTER)

kube-status:
	kubectl config current-context
	kubectl get pods -n $(DEMO_NAMESPACE)

dev-reset: dev-down dev-up
