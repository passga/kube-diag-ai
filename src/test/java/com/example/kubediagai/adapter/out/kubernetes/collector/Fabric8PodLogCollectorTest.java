package com.example.kubediagai.adapter.out.kubernetes.collector;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.adapter.out.kubernetes.mapper.PodLogFindingMapper;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import com.example.kubediagai.domain.Severity;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.ContainerResource;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Fabric8PodLogCollectorTest {

    private static final PodDiagnosticCommand COMMAND = new PodDiagnosticCommand("default", "api-0");

    @Test
    void collectsLogsForSingleContainerPod() {
        FakeLogClient client = new FakeLogClient(Map.of("app", "ready"));
        Fabric8PodLogCollector collector = new Fabric8PodLogCollector(client.client(), new PodLogFindingMapper());

        List<ClusterFinding> findings = collector.collect(COMMAND, podWithContainers(List.of("app"), List.of()));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("Recent logs for container app");
            assertThat(finding.details()).isEqualTo("ready");
        });
        assertThat(client.requestedContainers()).containsExactly("app");
        assertThat(client.tailedLines()).containsEntry("app", 80);
    }

    @Test
    void collectsLogsForMultiContainerPod() {
        FakeLogClient client = new FakeLogClient(Map.of(
                "app", "app log",
                "sidecar", "sidecar log"
        ));
        Fabric8PodLogCollector collector = new Fabric8PodLogCollector(client.client(), new PodLogFindingMapper());

        List<ClusterFinding> findings = collector.collect(
                COMMAND,
                podWithContainers(List.of("app", "sidecar"), List.of())
        );

        assertThat(findings)
                .extracting(ClusterFinding::message)
                .containsExactly("Recent logs for container app", "Recent logs for container sidecar");
        assertThat(client.requestedContainers()).containsExactly("app", "sidecar");
    }

    @Test
    void collectsLogsForInitContainers() {
        FakeLogClient client = new FakeLogClient(Map.of(
                "app", "app log",
                "init-db", "init log"
        ));
        Fabric8PodLogCollector collector = new Fabric8PodLogCollector(client.client(), new PodLogFindingMapper());

        List<ClusterFinding> findings = collector.collect(
                COMMAND,
                podWithContainers(List.of("app"), List.of("init-db"))
        );

        assertThat(findings)
                .extracting(ClusterFinding::message)
                .containsExactly("Recent logs for container app", "Recent logs for container init-db");
        assertThat(client.requestedContainers()).containsExactly("app", "init-db");
    }

    @Test
    void containerLogFailureDoesNotFailWholePodDiagnosis() {
        FakeLogClient client = new FakeLogClient(Map.of(
                "app", "app log",
                "sidecar", new KubernetesClientException("container log failed")
        ));
        Fabric8PodLogCollector collector = new Fabric8PodLogCollector(client.client(), new PodLogFindingMapper());

        List<ClusterFinding> findings = collector.collect(
                COMMAND,
                podWithContainers(List.of("app", "sidecar"), List.of())
        );

        assertThat(findings).hasSize(2);
        assertThat(findings)
                .anySatisfy(finding -> {
                    assertThat(finding.severity()).isEqualTo(Severity.INFO);
                    assertThat(finding.message()).isEqualTo("Recent logs for container app");
                })
                .anySatisfy(finding -> {
                    assertThat(finding.severity()).isEqualTo(Severity.WARNING);
                    assertThat(finding.message()).isEqualTo("Recent logs unavailable for container sidecar");
                    assertThat(finding.details()).isEqualTo("container log failed");
                });
        assertThat(client.requestedContainers()).containsExactly("app", "sidecar");
    }

    @Test
    void podWithNoContainersReturnsWarningFinding() {
        FakeLogClient client = new FakeLogClient(Map.of());
        Fabric8PodLogCollector collector = new Fabric8PodLogCollector(client.client(), new PodLogFindingMapper());

        List<ClusterFinding> findings = collector.collect(COMMAND, podWithContainers(List.of(), List.of()));

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.WARNING);
            assertThat(finding.message()).isEqualTo("Pod has no containers");
        });
        assertThat(client.requestedContainers()).isEmpty();
    }

    private static Pod podWithContainers(List<String> containerNames, List<String> initContainerNames) {
        return new PodBuilder()
                .withSpec(new PodSpecBuilder()
                        .withContainers(containerNames.stream()
                                .map(name -> new ContainerBuilder().withName(name).build())
                                .toList())
                        .withInitContainers(initContainerNames.stream()
                                .map(name -> new ContainerBuilder().withName(name).build())
                                .toList())
                        .build())
                .build();
    }

    private static final class FakeLogClient {

        private final Map<String, Object> containerLogs;
        private final List<String> requestedContainers = new ArrayList<>();
        private final Map<String, Integer> tailedLines = new LinkedHashMap<>();
        private String currentContainer;

        private FakeLogClient(Map<String, Object> containerLogs) {
            this.containerLogs = containerLogs;
        }

        private KubernetesClient client() {
            Object podOperation = proxy(MixedOperation.class, this::handlePodOperation);
            return proxy(KubernetesClient.class, (proxy, method, args) -> {
                if (method.getName().equals("pods")) {
                    return podOperation;
                }
                return handleObjectMethod(proxy, method.getName());
            });
        }

        private List<String> requestedContainers() {
            return requestedContainers;
        }

        private Map<String, Integer> tailedLines() {
            return tailedLines;
        }

        private Object handlePodOperation(Object proxy, java.lang.reflect.Method method, Object[] args) {
            if (method.getName().equals("inNamespace")) {
                assertThat(args[0]).isEqualTo(COMMAND.namespace());
                return proxy;
            }
            if (method.getName().equals("withName")) {
                assertThat(args[0]).isEqualTo(COMMAND.podName());
                return proxy(PodResource.class, this::handlePodResource);
            }
            return handleObjectMethod(proxy, method.getName());
        }

        private Object handlePodResource(Object proxy, java.lang.reflect.Method method, Object[] args) {
            if (method.getName().equals("inContainer")) {
                currentContainer = (String) args[0];
                requestedContainers.add(currentContainer);
                return proxy(ContainerResource.class, this::handleContainerResource);
            }
            return handleObjectMethod(proxy, method.getName());
        }

        private Object handleContainerResource(Object proxy, java.lang.reflect.Method method, Object[] args) {
            if (method.getName().equals("tailingLines")) {
                tailedLines.put(currentContainer, (Integer) args[0]);
                return proxy;
            }
            if (method.getName().equals("getLog")) {
                Object result = containerLogs.get(currentContainer);
                if (result instanceof KubernetesClientException exception) {
                    throw exception;
                }
                return result;
            }
            return handleObjectMethod(proxy, method.getName());
        }

        private static Object handleObjectMethod(Object proxy, String methodName) {
            return switch (methodName) {
                case "toString" -> "FakeLogClient";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> false;
                default -> throw new UnsupportedOperationException("Unexpected Fabric8 call: " + methodName);
            };
        }

        @SuppressWarnings("unchecked")
        private static <T> T proxy(Class<T> type, InvocationHandler handler) {
            return (T) Proxy.newProxyInstance(
                    type.getClassLoader(),
                    new Class<?>[]{type},
                    handler
            );
        }
    }
}
