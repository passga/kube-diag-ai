package com.example.kubediagai.adapter.in.rest;

import com.example.kubediagai.application.port.in.DiagnosePodUseCase;
import com.example.kubediagai.domain.DiagnosisResult;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pods")
public class PodDiagnosticController {

    private final DiagnosePodUseCase diagnosePodUseCase;

    public PodDiagnosticController(DiagnosePodUseCase diagnosePodUseCase) {
        this.diagnosePodUseCase = diagnosePodUseCase;
    }

    @Operation(
            summary = "Diagnose a Kubernetes Pod",
            description = "Collects read-only diagnostic findings for a Pod using its namespace and name."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Pod diagnostic request",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DiagnosePodRequest.class),
                    examples = @ExampleObject(
                            name = "CrashLoopBackOff pod",
                            value = """
                                    {
                                      "namespace": "demo",
                                      "podName": "pod-crashloop"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Successful diagnosis",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DiagnosisResult.class),
                            examples = @ExampleObject(
                                    name = "Pod diagnosis",
                                    value = """
                                            {
                                              "request": {
                                                "namespace": "demo",
                                                "podName": "pod-crashloop"
                                              },
                                              "findings": [
                                                {
                                                  "severity": "CRITICAL",
                                                  "message": "Container is waiting: CrashLoopBackOff",
                                                  "details": "crash: back-off restarting failed container"
                                                }
                                              ],
                                              "analysis": {
                                                "summary": "Placeholder analysis for Pod/pod-crashloop",
                                                "recommendations": [
                                                  "Wire a real AI provider adapter"
                                                ]
                                              },
                                              "createdAt": "2026-05-27T10:00:00Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @PostMapping("/diagnose")
    public DiagnosisResult diagnose(@Valid @RequestBody DiagnosePodRequest request) {
        return diagnosePodUseCase.diagnose(request.toCommand());
    }

    public record DiagnosePodRequest(
            @NotBlank String namespace,
            @NotBlank String podName
    ) {
        private PodDiagnosticCommand toCommand() {
            return new PodDiagnosticCommand(namespace, podName);
        }
    }
}
