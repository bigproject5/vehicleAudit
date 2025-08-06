package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.event.AiDiagnosisCompletedEventDTO;
import aivle.project.vehicleAudit.repository.InspectionRepository;
import aivle.project.vehicleAudit.domain.Inspection;
import aivle.project.vehicleAudit.domain.enumerate.InspectionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiDiagnosisCompletedEventConsumer {

    private final InspectionRepository inspectionRepository;

    @KafkaListener(topics = "ai-diagnosis-completed", groupId = "vehicle-audit-group")
    @Transactional
    public void consume(AiDiagnosisCompletedEventDTO event) {
        log.info("Received AI diagnosis completed event: {}", event);

        Inspection inspection = inspectionRepository.findById(event.getInspectionId())
                .orElseThrow(() -> new RuntimeException("검사 ID 없음: " + event.getInspectionId()));

        // AI 결과 반영
        inspection.setDefect(event.isDefect());
        inspection.setAiSuggestion(event.getAiSuggestion());
        inspection.setResultDataPath(event.getResultDataPath());
        inspection.setDiagnosisResult(event.getDiagnosisResult());
        inspection.setStatus(
                event.isDefect() ? InspectionStatus.ABNORMAL : InspectionStatus.NORMAL
        );
        inspectionRepository.save(inspection);

    }
}
