package aivle.project.vehicleAudit.rest.dto;

public record AuditSummaryDTO(
        String auditId,
        String model,
        String lineCode,
        String testAt,
        String status
) {
}
