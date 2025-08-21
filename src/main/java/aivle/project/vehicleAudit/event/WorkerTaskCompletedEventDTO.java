package aivle.project.vehicleAudit.event;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkerTaskCompletedEventDTO {
    private String workerName;
    private String resolve; // 조치사항 등
    private Long auditId;
    private Long inspectionId;
    private Long workerId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String type;
    private String aiSuggestion;      // AI 조치제안
    private String diagnosisResult;   // 검사결과
    private String resultDataPath;
}