package aivle.project.vehicleAudit.rest.dto;

import java.time.LocalDateTime;

public record AuditSummaryDTO(
        String auditId,
        String model,
        String lineCode,
        LocalDateTime testAt,
        String status
) {
}
