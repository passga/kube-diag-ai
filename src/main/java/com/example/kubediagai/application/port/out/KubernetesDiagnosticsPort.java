package com.example.kubediagai.application.port.out;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import java.util.List;

public interface KubernetesDiagnosticsPort {

    List<ClusterFinding> collectPodFindings(PodDiagnosticCommand command);
}
