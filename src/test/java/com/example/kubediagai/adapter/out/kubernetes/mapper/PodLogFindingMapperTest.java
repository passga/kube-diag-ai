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
        List<ClusterFinding> findings = mapper.map("""
                first line
                second line
                """);

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("Recent pod logs");
            assertThat(finding.details()).contains("first line", "second line");
        });
    }

    @Test
    void mapsBlankLogsToEmptyFinding() {
        List<ClusterFinding> findings = mapper.map("   ");

        assertThat(findings).singleElement().satisfies(finding -> {
            assertThat(finding.severity()).isEqualTo(Severity.INFO);
            assertThat(finding.message()).isEqualTo("Recent pod logs are empty");
        });
    }
}
