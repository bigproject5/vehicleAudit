package aivle.project.vehicleAudit.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

public record AuditManualDTO(
        String model,
        String lineCode
) {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class InspectionFileDTO {
        private String inspectionType;
        private MultipartFile collectDataFile;
    }
}
