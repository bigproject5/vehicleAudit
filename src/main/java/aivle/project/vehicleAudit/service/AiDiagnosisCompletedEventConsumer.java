package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.domain.Audit;
import aivle.project.vehicleAudit.domain.enumerate.AuditStatus;
import aivle.project.vehicleAudit.event.AiDiagnosisCompletedEventDTO;
import aivle.project.vehicleAudit.repository.AuditRepository;
import aivle.project.vehicleAudit.domain.Inspection;
import aivle.project.vehicleAudit.domain.enumerate.InspectionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import aivle.project.vehicleAudit.rest.dto.RagSuggestRequest;
import aivle.project.vehicleAudit.rest.dto.RagSuggestResponse;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiDiagnosisCompletedEventConsumer {
    private final RagService ragService;
    private final ObjectMapper objectMapper;

    private final AuditRepository auditRepository;

    @KafkaListener(topics = "ai-diagnosis-completed", groupId = "vehicle-audit-group")
    @Transactional
    public void consume(AiDiagnosisCompletedEventDTO event) {
        log.info("Received AI diagnosis completed event: {}", event);
        Audit audit = auditRepository.findWithInspectionsById(event.getAuditId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 테스트 ID: " + event.getAuditId()));
        Inspection targetInspection = audit.getInspections().stream()
                .filter(inspection -> inspection.getId().equals(event.getInspectionId()))
                .findFirst()
                .orElseThrow(
                        () -> new RuntimeException("존재하지 않는 점검 ID: " + event.getInspectionId())
                );

        // AI 결과 반영
        targetInspection.setDefect(event.isDefect());
        {
            if (event.isDefect()) {
                try {
                    aivle.project.vehicleAudit.rest.dto.RagSuggestRequest req = new RagSuggestRequest();
                    req.setProcess(targetInspection.getInspectionType().name());
                    req.setDefect(true);
                    req.setConfidence(null);
                    req.setModelVersion(null);
                    req.setInspectionId(targetInspection.getId());
                    req.setVehicleModel(null);
                    RagSuggestResponse rag = ragService.suggest(req);
                    targetInspection.setAiSuggestion(rag.getActions().getFirst());
                } catch (Exception ex) {
                    log.error("RAG suggestion failed for inspection ID {}: {}", targetInspection.getId(), ex.getMessage());
                    targetInspection.setAiSuggestion(null);
                }
            } else {
                if (audit.allInspectionsCompleted()) {
                    audit.setStatus(AuditStatus.COMPLETED);
                }
            }
        }
        targetInspection.setResultDataPath(event.getResultDataPath());
        targetInspection.setDiagnosisResult(event.getDiagnosisResult());
        targetInspection.setStatus(
                event.isDefect() ? InspectionStatus.ABNORMAL : InspectionStatus.NORMAL
        );
        auditRepository.save(audit);
    }
}
