package com.example.kubediagai.adapter.out.kubernetes.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.kubediagai.domain.ClusterFinding;
import com.example.kubediagai.domain.Severity;
import java.util.List;
import org.junit.jupiter.api.Test;

class PodLogFindingMapperTest {

    private final PodLogFindingMapper mapper = new PodLogFindingMapper();

    @Test
    void mapsRecentLogsToInfoFinding() {
        List<ClusterFinding> findings = mapper.map("app", """
                first line
                second line
                """);

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("Recent logs for container app");
            assertThat(finding.details()).contains("first line", "second line");
        });
    }

    @Test
    void mapsBlankLogsToEmptyFinding() {
        List<ClusterFinding> findings = mapper.map("app", "   ");

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("Recent logs are empty for container app");
        });
    }

    @Test
    void mapsUnavailableLogsToWarningFinding() {
        ClusterFinding finding = mapper.mapUnavailable(
                "app",
                new io.fabric8.kubernetes.client.KubernetesClientException("failed")
        );

        assertThat(finding.severity()).isEqualTo(Severity.WARNING);
        assertThat(finding.message()).isEqualTo("Recent logs unavailable for container app");
        assertThat(finding.details()).isEqualTo("failed");
    }

    @Test
    void mapsNoContainersToWarningFinding() {
        ClusterFinding finding = mapper.mapNoContainers();

        assertThat(finding.severity()).isEqualTo(Severity.WARNING);
        assertThat(finding.message()).isEqualTo("Pod has no containers");
        assertThat(finding.details()).contains("No regular or init containers");
    }
}
