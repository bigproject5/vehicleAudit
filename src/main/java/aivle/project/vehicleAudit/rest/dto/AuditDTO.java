package aivle.project.vehicleAudit.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuditDTO(
        String auditId,
        String model,
        String lineCode,
        String testAt,
        String status,
        List<InspectionSummaryDTO> inspections
) {
}