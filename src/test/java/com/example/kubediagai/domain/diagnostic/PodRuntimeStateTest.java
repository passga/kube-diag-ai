package com.example.kubediagai.domain.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PodRuntimeStateTest {

    @Test
    void should_sum_restart_counts_when_multiple_containers_are_present() {
        PodRuntimeState state = pod(
                container(1, null),
                container(2, null),
                container(0, null)
        );

        assertThat(state.restartCount()).isEqualTo(3);
    }

    @Test
    void should_collect_waiting_reasons_when_containers_are_waiting() {
        PodRuntimeState state = pod(
                container(0, "ContainerCreating"),
                container(0, null),
                container(0, "PodInitializing")
        );

        assertThat(state.waitingReasons()).containsExactly("ContainerCreating", "PodInitializing");
    }

    @Test
    void should_select_unhealthy_waiting_reason_when_one_is_present() {
        PodRuntimeState state = pod(
                container(0, "ContainerCreating"),
                container(2, "CrashLoopBackOff")
        );

        assertThat(state.selectedWaitingReason()).isEqualTo("CrashLoopBackOff");
        assertThat(state.hasUnhealthyWaitingReason()).isTrue();
    }

    @Test
    void should_select_first_waiting_reason_when_no_unhealthy_reason_exists() {
        PodRuntimeState state = pod(
                container(0, "ContainerCreating"),
                container(0, "PodInitializing")
        );

        assertThat(state.selectedWaitingReason()).isEqualTo("ContainerCreating");
        assertThat(state.hasUnhealthyWaitingReason()).isFalse();
    }

    @Test
    void should_select_no_waiting_reason_when_no_container_is_waiting() {
        PodRuntimeState state = pod(
                container(0, null),
                container(0, null)
        );

        assertThat(state.selectedWaitingReason()).isNull();
        assertThat(state.waitingReasons()).isEmpty();
    }

    @Test
    void should_ignore_blank_waiting_reason_when_container_state_is_created() {
        PodRuntimeState state = pod(
                container(0, " ")
        );

        assertThat(state.selectedWaitingReason()).isNull();
        assertThat(state.waitingReasons()).isEmpty();
    }

    private static PodRuntimeState pod(PodContainerRuntimeState... containers) {
        return new PodRuntimeState("Running", true, List.of(containers), false);
    }

    private static PodContainerRuntimeState container(int restartCount, String waitingReason) {
        return new PodContainerRuntimeState(restartCount, waitingReason);
    }
}
