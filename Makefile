K3D_CLUSTER ?= kube-diag-dev
KUBE_CONTEXT ?= k3d-$(K3D_CLUSTER)
DEMO_NAMESPACE ?= demo
APP_URL ?= http://localhost:8080
FIXTURE_TIMEOUT_SECONDS ?= 120

.PHONY: dev-up run smoke-test smoke-test-pod-summary dev-down kube-status dev-reset wait-fixtures

dev-up:
	k3d cluster list $(K3D_CLUSTER) >/dev/null 2>&1 || k3d cluster create --config local/k3d/cluster.yaml --wait
	kubectl config use-context $(KUBE_CONTEXT)
	kubectl apply -f local/fixtures/namespace.yaml
	kubectl apply -f local/fixtures/pod-ok.yaml
	kubectl apply -f local/fixtures/pod-crashloop.yaml
	kubectl apply -f local/fixtures/pod-imagepullbackoff.yaml
	$(MAKE) wait-fixtures
	kubectl get pods -n $(DEMO_NAMESPACE)

run:
	mvn spring-boot:run

smoke-test: wait-fixtures
	curl -fsS -X POST $(APP_URL)/api/pods/diagnose -H 'Content-Type: application/json' -d '{"namespace":"$(DEMO_NAMESPACE)","podName":"pod-ok"}'
	curl -fsS -X POST $(APP_URL)/api/pods/diagnose -H 'Content-Type: application/json' -d '{"namespace":"$(DEMO_NAMESPACE)","podName":"pod-crashloop"}'
	curl -fsS -X POST $(APP_URL)/api/pods/diagnose -H 'Content-Type: application/json' -d '{"namespace":"$(DEMO_NAMESPACE)","podName":"pod-imagepullbackoff"}'

smoke-test-pod-summary: wait-fixtures
	@response_file=$$(mktemp); \
	trap 'rm -f "$$response_file"' EXIT; \
	curl -fsS -X GET $(APP_URL)/api/namespaces/$(DEMO_NAMESPACE)/pods > "$$response_file"; \
	grep -q '"name":"pod-ok"' "$$response_file"; \
	grep -q '"name":"pod-crashloop"' "$$response_file"; \
	grep -q '"name":"pod-imagepullbackoff"' "$$response_file"; \
	grep -q '"healthStatus":"' "$$response_file"; \
	grep -q '"healthStatus":"UNHEALTHY"' "$$response_file"; \
	grep -Eq '"name":"pod-crashloop".*"waitingReason":"CrashLoopBackOff"' "$$response_file"; \
	grep -Eq '"name":"pod-imagepullbackoff".*"waitingReason":"(ImagePullBackOff|ErrImagePull)"' "$$response_file"; \
	cat "$$response_file"

wait-fixtures:
	@echo "Waiting for demo pod fixtures in namespace $(DEMO_NAMESPACE) (timeout: $(FIXTURE_TIMEOUT_SECONDS)s)"
	@deadline=$$(($$(date +%s) + $(FIXTURE_TIMEOUT_SECONDS))); \
	while [ "$$(date +%s)" -lt "$$deadline" ]; do \
		phase=$$(kubectl get pod pod-ok -n $(DEMO_NAMESPACE) -o jsonpath='{.status.phase}' 2>/dev/null || true); \
		ready=$$(kubectl get pod pod-ok -n $(DEMO_NAMESPACE) -o jsonpath='{.status.conditions[?(@.type=="Ready")].status}' 2>/dev/null || true); \
		if [ "$$phase" = "Running" ] || [ "$$ready" = "True" ]; then \
			echo "pod-ok reached phase=$$phase ready=$$ready"; \
			break; \
		fi; \
		sleep 2; \
	done; \
	if [ "$$phase" != "Running" ] && [ "$$ready" != "True" ]; then \
		echo "Timed out waiting for pod-ok to be Running or Ready"; \
		kubectl get pod pod-ok -n $(DEMO_NAMESPACE) -o wide || true; \
		exit 1; \
	fi
	@deadline=$$(($$(date +%s) + $(FIXTURE_TIMEOUT_SECONDS))); \
	while [ "$$(date +%s)" -lt "$$deadline" ]; do \
		reason=$$(kubectl get pod pod-crashloop -n $(DEMO_NAMESPACE) -o jsonpath='{.status.containerStatuses[0].state.waiting.reason}' 2>/dev/null || true); \
		restarts=$$(kubectl get pod pod-crashloop -n $(DEMO_NAMESPACE) -o jsonpath='{.status.containerStatuses[0].restartCount}' 2>/dev/null || true); \
		if [ "$$reason" = "CrashLoopBackOff" ] || [ "$${restarts:-0}" -gt 0 ] 2>/dev/null; then \
			echo "pod-crashloop reached waitingReason=$$reason restarts=$$restarts"; \
			break; \
		fi; \
		sleep 2; \
	done; \
	if [ "$$reason" != "CrashLoopBackOff" ] && ! [ "$${restarts:-0}" -gt 0 ] 2>/dev/null; then \
		echo "Timed out waiting for pod-crashloop to reach CrashLoopBackOff or restart"; \
		kubectl get pod pod-crashloop -n $(DEMO_NAMESPACE) -o wide || true; \
		exit 1; \
	fi
	@deadline=$$(($$(date +%s) + $(FIXTURE_TIMEOUT_SECONDS))); \
	while [ "$$(date +%s)" -lt "$$deadline" ]; do \
		reason=$$(kubectl get pod pod-imagepullbackoff -n $(DEMO_NAMESPACE) -o jsonpath='{.status.containerStatuses[0].state.waiting.reason}' 2>/dev/null || true); \
		if [ "$$reason" = "ImagePullBackOff" ] || [ "$$reason" = "ErrImagePull" ]; then \
			echo "pod-imagepullbackoff reached waitingReason=$$reason"; \
			break; \
		fi; \
		sleep 2; \
	done; \
	if [ "$$reason" != "ImagePullBackOff" ] && [ "$$reason" != "ErrImagePull" ]; then \
		echo "Timed out waiting for pod-imagepullbackoff to reach ImagePullBackOff or ErrImagePull"; \
		kubectl get pod pod-imagepullbackoff -n $(DEMO_NAMESPACE) -o wide || true; \
		exit 1; \
	fi

dev-down:
	k3d cluster delete $(K3D_CLUSTER)

kube-status:
	kubectl config current-context
	kubectl get pods -n $(DEMO_NAMESPACE)

dev-reset: dev-down dev-up
