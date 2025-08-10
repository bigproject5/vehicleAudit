package aivle.project.vehicleAudit.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestStartedEventDTO {
    private Long auditId;
    private String model;
    private String lineCode;
    private String inspectionType;
    private String collectDataPath;
}
