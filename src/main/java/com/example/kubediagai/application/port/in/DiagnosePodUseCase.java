package com.example.kubediagai.application.port.in;

import com.example.kubediagai.domain.DiagnosisResult;
import com.example.kubediagai.domain.PodDiagnosticCommand;

public interface DiagnosePodUseCase {

    DiagnosisResult diagnose(PodDiagnosticCommand command);
}
