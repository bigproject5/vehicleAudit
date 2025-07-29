package aivle.project.vehicleAudit.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuditDTO(
        Long auditId,
        String model,
        String lineCode,
        LocalDateTime testAt,
        String status,
        List<InspectionSummaryDTO> inspections
) {
}