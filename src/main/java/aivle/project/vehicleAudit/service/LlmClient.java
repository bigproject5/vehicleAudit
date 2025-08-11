package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.rest.dto.RagSuggestRequest;
import aivle.project.vehicleAudit.rest.dto.RagSuggestResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LlmClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.chat-model:gpt-4o-mini}")
    private String chatModel;

    public RagSuggestResponse generate(RagSuggestRequest req, String chunksText, Map<String,Object> schema) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI api key not configured");
        }
        String system = "너는 자동차 생산 라인 공정 전문가다. 제공된 '근거 청크'만을 사용하여 공정별 1차 점검·조치 트리아지를 JSON 스키마로 출력한다. 문서 출처/버전을 포함하고, 추측은 금지한다.";
        String user = String.format("""
[입력]
process: %s
is_defect: %s
confidence: %s

[근거 청크]
%s

[출력 스키마]
%s
""", req.getProcess(), req.isDefect(), String.valueOf(req.getConfidence()), chunksText, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();
        Map<String, Object> body = Map.of(
                "model", chatModel,
                "messages", List.of(
                        Map.of("role","system","content",system),
                        Map.of("role","user","content",user)
                ),
                "temperature", 0.2
        );
        Map<?,?> resp = client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(apiKey))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        @SuppressWarnings("unchecked")
        List<?> choices = (List<?>) resp.get("choices");
        @SuppressWarnings("unchecked")
        Map<String,Object> choice0 = (Map<String,Object>) choices.get(0);
        @SuppressWarnings("unchecked")
        Map<String,Object> message = (Map<String,Object>) choice0.get("message");
        String text = message != null ? String.valueOf(message.get("content")) : null;

        @SuppressWarnings("unchecked")
        Map<String,Object> map = objectMapper.readValue(text, new TypeReference<Map<String,Object>>(){});

        RagSuggestResponse out = new RagSuggestResponse();
        out.setLevel(String.valueOf(map.getOrDefault("level","TRIAGE")));
        out.setTitle(String.valueOf(map.getOrDefault("title", req.getProcess()+" 공정 1차 점검(트리아지)")));
        out.setActions((List<String>) map.getOrDefault("actions", List.of()));
        out.setTools((List<String>) map.getOrDefault("tools", List.of()));
        out.setParts((List<String>) map.getOrDefault("parts", List.of()));
        if (map.get("time_min") != null) out.setTimeMin(((Number)map.get("time_min")).intValue());
        out.setVerification((List<String>) map.getOrDefault("verification", List.of()));
        out.setSafety((List<String>) map.getOrDefault("safety", List.of()));
        out.setSources((List<Map<String,String>>) map.getOrDefault("sources", List.of()));
        if (map.get("overall_confidence") != null) out.setOverallConfidence(((Number)map.get("overall_confidence")).doubleValue());
        if (map.get("need_human_review") != null) out.setNeedHumanReview((Boolean) map.get("need_human_review"));
        return out;
    }
}
