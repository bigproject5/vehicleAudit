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
import aivle.project.vehicleAudit.service.RagClient;
import aivle.project.vehicleAudit.rest.dto.RagSuggestRequest;
import aivle.project.vehicleAudit.rest.dto.RagSuggestResponse;
import org.springframework.transaction.annotation.Transactional;
import aivle.project.vehicleAudit.domain.enumerate.SuggestionLevel;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiDiagnosisCompletedEventConsumer {
    private final RagClient ragClient;
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
            String incoming = event.getAiSuggestion();
            if (event.isDefect() && (incoming == null || incoming.isBlank())) {
                try {
                    aivle.project.vehicleAudit.rest.dto.RagSuggestRequest req = new RagSuggestRequest();
                    req.setProcess(inspection.getType().name());
                    req.setDefect(true);
                    req.setConfidence(null);
                    req.setModelVersion(null);
                    req.setInspectionId(inspection.getId());
                    req.setVehicleModel(null);
                    RagSuggestResponse rag = ragClient.suggest(req);
                    incoming = objectMapper.writeValueAsString(rag);
                } catch (Exception ex) {
                    incoming = "{\"level\":\"TRIAGE\",\"title\":\"기본 점검 안내\",\"actions\":[\"문서 조회 중입니다. 잠시 후 다시 시도해주세요.\"],\"overall_confidence\":0.0,\"need_human_review\":true}";
                }
            }
            inspection.setAiSuggestion(incoming);
        if (rag != null) {
            inspection.setAiSuggestionLevel("SPECIFIC".equalsIgnoreCase(rag.getLevel()) ? SuggestionLevel.SPECIFIC : SuggestionLevel.TRIAGE);
            inspection.setAiSuggestionConfidence(rag.getOverallConfidence());
            try { inspection.setAiSuggestionSources(objectMapper.writeValueAsString(rag.getSources())); } catch (Exception ignore) {}
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
