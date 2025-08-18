package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.event.AiDiagnosisCompletedEventDTO;
import aivle.project.vehicleAudit.repository.InspectionRepository;
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
import aivle.project.vehicleAudit.domain.enumerate.SuggestionLevel;
import aivle.project.vehicleAudit.service.RagService;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiDiagnosisCompletedEventConsumer {
    private final RagService ragService;
    private final ObjectMapper objectMapper;

    private final InspectionRepository inspectionRepository;

    @KafkaListener(topics = "ai-diagnosis-completed", groupId = "vehicle-audit-group")
    @Transactional
    public void consume(AiDiagnosisCompletedEventDTO event) {
        log.info("Received AI diagnosis completed event: {}", event);

        Inspection inspection = inspectionRepository.findById(event.getInspectionId())
                .orElseThrow(() -> new RuntimeException("검사 ID 없음: " + event.getInspectionId()));

        // AI 결과 반영
        inspection.setDefect(event.isDefect());
        {
            if (event.isDefect()) {
                try {
                    aivle.project.vehicleAudit.rest.dto.RagSuggestRequest req = new RagSuggestRequest();
                    req.setProcess(inspection.getInspectionType().name());
                    req.setDefect(true);
                    req.setConfidence(null);
                    req.setModelVersion(null);
                    req.setInspectionId(inspection.getId());
                    req.setVehicleModel(null);
                    RagSuggestResponse rag = ragService.suggest(req);
                    inspection.setAiSuggestion(rag.getActions().getFirst());
                } catch (Exception ex) {
                    log.error("RAG suggestion failed for inspection ID {}: {}", inspection.getId(), ex.getMessage());
                    inspection.setAiSuggestion(null);
                }
            }
        }
        inspection.setResultDataPath(event.getResultDataPath());
        inspection.setDiagnosisResult(event.getDiagnosisResult());
        inspection.setStatus(
                event.isDefect() ? InspectionStatus.ABNORMAL : InspectionStatus.NORMAL
        );
        inspectionRepository.save(inspection);

    }
}
