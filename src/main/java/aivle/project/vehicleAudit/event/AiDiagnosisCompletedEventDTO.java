package aivle.project.vehicleAudit.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiDiagnosisCompletedEventDTO {
    private Long inspectionId;
    private boolean isDefect;
    private String aiSuggestion;
    private String resultDataPath;
    private String diagnosisResult;
}
