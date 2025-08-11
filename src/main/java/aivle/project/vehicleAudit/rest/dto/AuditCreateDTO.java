package aivle.project.vehicleAudit.rest.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

public record AuditCreateDTO(
    String model,
    String lineCode,
    List<InspectionCreateDTO> inspections
) {
    @Getter
    @Setter
    public static class InspectionCreateDTO {
        String Type;
        String collectDataPath;
    }
}
