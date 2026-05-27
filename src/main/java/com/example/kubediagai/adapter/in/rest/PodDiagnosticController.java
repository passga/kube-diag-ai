package com.example.kubediagai.adapter.in.rest;

import com.example.kubediagai.application.port.in.DiagnosePodUseCase;
import com.example.kubediagai.domain.DiagnosisResult;
import com.example.kubediagai.domain.PodDiagnosticCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pods")
public class PodDiagnosticController {

    private final DiagnosePodUseCase diagnosePodUseCase;

    public PodDiagnosticController(DiagnosePodUseCase diagnosePodUseCase) {
        this.diagnosePodUseCase = diagnosePodUseCase;
    }

    @PostMapping("/diagnose")
    @ResponseStatus(HttpStatus.ACCEPTED)
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
