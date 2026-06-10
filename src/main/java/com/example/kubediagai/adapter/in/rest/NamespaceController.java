package com.example.kubediagai.adapter.in.rest;

import com.example.kubediagai.adapter.in.rest.dto.NamespaceResponse;
import com.example.kubediagai.application.port.in.ListNamespacePodsUseCase;
import com.example.kubediagai.application.port.in.ListNamespacesUseCase;
import com.example.kubediagai.domain.PodSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/namespaces")
public class NamespaceController {

    private final ListNamespacePodsUseCase listNamespacePodsUseCase;
    private final ListNamespacesUseCase listNamespacesUseCase;

    public NamespaceController(ListNamespacePodsUseCase listNamespacePodsUseCase, ListNamespacesUseCase listNamespacesUseCase) {
        this.listNamespacePodsUseCase = listNamespacePodsUseCase;
        this.listNamespacesUseCase = listNamespacesUseCase;
    }

    @Operation(
            summary = "List namespaces",
            description = "Returns visible Kubernetes namespaces with their current status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Namespaces",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NamespaceResponse.class)),
                            examples = @ExampleObject(
                                    name = "Namespaces",
                                    value = """
                                            [
                                              {
                                                "name": "demo",
                                                "status": "Active"
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping
    public List<NamespaceResponse> listNamespaces() {
        return listNamespacesUseCase.listNamespaces()
                .stream()
                .map(NamespaceResponse::from)
                .toList();
    }

    @Operation(
            summary = "List Pods in a namespace",
            description = "Returns read-only Pod summaries with diagnostic health status for a namespace."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pod summaries",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PodSummary.class)),
                            examples = @ExampleObject(
                                    name = "Namespace Pod summaries",
                                    value = """
                                            [
                                              {
                                                "namespace": "demo",
                                                "name": "pod-crashloop",
                                                "phase": "Running",
                                                "ready": false,
                                                "restartCount": 5,
                                                "waitingReason": "CrashLoopBackOff",
                                                "healthStatus": "UNHEALTHY"
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping("/{namespace}/pods")
    public List<PodSummary> listPods(@PathVariable @NotBlank String namespace) {
        return listNamespacePodsUseCase.listPods(namespace);
    }
}
