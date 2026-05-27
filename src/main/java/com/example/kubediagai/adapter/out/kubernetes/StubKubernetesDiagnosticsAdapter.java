package com.example.kubediagai.adapter.out.kubernetes;

import com.example.kubediagai.application.port.out.KubernetesDiagnosticsPort;
import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import com.example.kubediagai.domain.Severity;
import java.util.List;

public class StubKubernetesDiagnosticsAdapter implements KubernetesDiagnosticsPort {

    @Override
    public List<ClusterFinding> collectPodFindings(PodDiagnosticCommand command) {
        return List.of(new ClusterFinding(
                Severity.INFO,
                "No Kubernetes client configured",
                "Stub adapter received pod " + command.namespace() + "/" + command.podName()
        ));
    }
}
