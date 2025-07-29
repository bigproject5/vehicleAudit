package aivle.project.vehicleAudit.rest.dto;

import java.util.List;

public record AuditCreateDTO(
    String model,
    String lineCode,
    List<InspectionCreateDTO> inspections
) {
    public static class InspectionCreateDTO {
        String inspectionType;
        String collectDataPath;
    }
}
