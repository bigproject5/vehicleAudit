package aivle.project.vehicleAudit.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InspectionSummaryDTO(
        String inspectionId,
        String auditId,
        String inspectionType,
        String status,
        String collectDataPath,
        boolean isDefect,
        Long workerId,
        String workerName,
        LocalDateTime taskStartedAt,
        LocalDateTime taskFinishedAt,
        String collectDataPath
) {
}
