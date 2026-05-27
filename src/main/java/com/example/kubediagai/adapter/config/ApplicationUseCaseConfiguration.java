package com.example.kubediagai.adapter.config;

import com.example.kubediagai.application.port.in.DiagnosePodUseCase;
import com.example.kubediagai.application.port.out.AiAnalysisPort;
import com.example.kubediagai.application.port.out.KubernetesDiagnosticsPort;
import com.example.kubediagai.application.service.DiagnosticAssistantService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationUseCaseConfiguration {

    @Bean
    DiagnosePodUseCase diagnosePodUseCase(
            KubernetesDiagnosticsPort kubernetesDiagnosticsPort,
            AiAnalysisPort aiAnalysisPort
    ) {
        return new DiagnosticAssistantService(kubernetesDiagnosticsPort, aiAnalysisPort);
    }
}
