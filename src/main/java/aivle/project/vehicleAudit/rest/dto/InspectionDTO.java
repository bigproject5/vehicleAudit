package aivle.project.vehicleAudit.rest.dto;

import aivle.project.vehicleAudit.domain.Task;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InspectionDTO(
        String inspectionId,
        String auditId,
        String type,
        String status,
        boolean isDefect,
        String collectDataPath,
        String resultDataPath,
        String aiSuggestion,
        String diagnosisResult,
        String resolve,
        Task task
) {
}
