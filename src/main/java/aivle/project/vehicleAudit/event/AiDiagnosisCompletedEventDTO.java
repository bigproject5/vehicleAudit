package aivle.project.vehicleAudit.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiDiagnosisCompletedEventDTO {
    private Long auditId;
    private Long inspectionId;
    private String inspectionType;
    private boolean isDefect;
    private String collectDataPath;
    private String resultDataPath;
    private String diagnosisResult;
}
