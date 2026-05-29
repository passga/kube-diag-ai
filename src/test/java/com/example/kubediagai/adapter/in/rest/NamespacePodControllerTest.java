package com.example.kubediagai.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.kubediagai.application.port.in.ListNamespacePodsUseCase;
import com.example.kubediagai.domain.PodHealthStatus;
import com.example.kubediagai.domain.PodSummary;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class NamespacePodControllerTest {

    private final StubListNamespacePodsUseCase useCase = new StubListNamespacePodsUseCase();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new NamespacePodController(useCase)).build();
    }

    @Test
    void listsNamespacePods() throws Exception {
        useCase.result = List.of(new PodSummary(
                "demo",
                "pod-crashloop",
                "Running",
                false,
                5,
                "CrashLoopBackOff",
                PodHealthStatus.UNHEALTHY
        ));

        mockMvc.perform(get("/api/namespaces/demo/pods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].namespace").value("demo"))
                .andExpect(jsonPath("$[0].name").value("pod-crashloop"))
                .andExpect(jsonPath("$[0].phase").value("Running"))
                .andExpect(jsonPath("$[0].ready").value(false))
                .andExpect(jsonPath("$[0].restartCount").value(5))
                .andExpect(jsonPath("$[0].waitingReason").value("CrashLoopBackOff"))
                .andExpect(jsonPath("$[0].healthStatus").value("UNHEALTHY"));

        assertThat(useCase.namespace).isEqualTo("demo");
    }

    private static class StubListNamespacePodsUseCase implements ListNamespacePodsUseCase {

        private List<PodSummary> result = List.of();
        private String namespace;

        @Override
        public List<PodSummary> listPods(String namespace) {
            this.namespace = namespace;
            return result;
        }
    }
}
