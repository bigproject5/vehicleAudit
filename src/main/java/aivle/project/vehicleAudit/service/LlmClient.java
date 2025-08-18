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

    /**
     * 자연어 형태의 조치 안내문 생성
     */
    public String generateText(RagSuggestRequest req, String chunksText) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI api key not configured");
        }

        String system = """
당신은 자동차 생산 라인의 숙련된 현장 선배 기술자입니다. 
후배 작업자에게 말로 설명해주듯이 친근하고 자연스럽게 조치 방법을 안내해주세요.

각 단계를 구체적이고 상세하게 설명하고, 왜 그 작업을 해야 하는지, 
어떻게 해야 하는지를 초보자도 이해할 수 있도록 친절하게 안내해주세요.

예시:
"먼저 워셔액 탱크를 확인해주세요. 탱크가 비어있거나 부족하다면 
깔때기를 사용하여 권장 워셔액을 천천히 보충해주시기 바랍니다. 
액이 넘치지 않도록 주의하면서 MAX 라인까지 채워주세요."

이런 식으로 각 단계를 자세하고 친근하게 설명해주세요.
절대 JSON 형태로 출력하지 마시고, 일반 문장으로만 작성해주세요.
""";

        String user = String.format("""
%s 공정에서 문제가 생겼습니다. 

참고할 수 있는 매뉴얼 내용:
%s

위 내용을 참고해서 현장 작업자가 바로 실행할 수 있도록 친근하게 설명해주세요.
마치 옆에서 직접 알려주는 것처럼 자연스럽게 말해주세요.

특수문자나 번호 목록 없이 그냥 평상시 말하듯이 설명해주시면 됩니다.
""",
                req.getProcess(),
                chunksText);

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();
        Map<String, Object> body = Map.of(
                "model", chatModel,
                "messages", List.of(
                        Map.of("role","system","content",system),
                        Map.of("role","user","content",user)
                ),
                "temperature", 0.4, // 약간 높여서 더 자연스럽게
                "max_tokens", 1000  // 충분한 토큰 수
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
        String text = message != null ? String.valueOf(message.get("content")) : "";

        // 혹시 JSON 형태로 응답이 왔을 경우 감지하고 재요청
        if (text.trim().startsWith("{") || text.trim().startsWith("[")) {
            // JSON 형태 응답일 경우 다시 요청 (더 강한 프롬프트 사용)
            return generatePlainTextFallback(req, chunksText);
        }

        return text.trim();
    }

    /**
     * 더 강력한 자연어 생성 (JSON 방지)
     */
    private String generatePlainTextFallback(RagSuggestRequest req, String chunksText) throws Exception {
        String system = """
당신은 현장 작업자에게 말로 설명해주는 베테랑 기술자입니다.
절대로 JSON, 코드, 구조화된 데이터를 사용하지 마세요.
오직 일반 텍스트로만 답변하세요.
""";

        String user = String.format("""
%s 공정에서 문제가 생겼어요. 어떻게 해결하면 될까요?

참고할 수 있는 자료:
%s

위 자료를 보고 현장에서 바로 실행할 수 있게 쉽게 설명해주세요.
일반 문장으로만 작성하고, 중괄호나 대괄호는 절대 사용하지 마세요.
""", req.getProcess(), chunksText);

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();
        Map<String, Object> body = Map.of(
                "model", chatModel,
                "messages", List.of(
                        Map.of("role","system","content",system),
                        Map.of("role","user","content",user)
                ),
                "temperature", 0.6
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

        return message != null ? String.valueOf(message.get("content")).trim() :
                "문서 내용을 바탕으로 해당 공정의 표준 절차에 따라 점검을 진행해주세요.";
    }

    // 기존 메서드는 유지 (호환성을 위해)
    public RagSuggestResponse generate(RagSuggestRequest req, String chunksText, Map<String,Object> schema) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI api key not configured");
        }

        String system = """
당신은 자동차 정비소의 베테랑 정비사입니다. 
신입 정비사에게 직접 말로 설명해주듯이 자연스럽게 이야기해주세요.

중요한 규칙:
- 절대로 번호나 목록을 사용하지 마세요 (1., 2., -, * 등 금지)
- 이모지나 특수문자 사용 금지 (**,🔧,✅ 등 금지)  
- 그냥 평상시 말하듯이 자연스럽게 연결해서 설명
- "먼저~하고, 그 다음에~하세요, 마지막으로~확인해주세요" 이런 식으로

예시:
"워셔액이 제대로 나오지 않는 문제가 생겼네요. 먼저 워셔액 탱크를 확인해보세요. 
탱크가 비어있다면 깔때기를 사용해서 천천히 워셔액을 보충해주시고요. 
그 다음에는 노즐이 막혔는지 확인해보세요. 핀셋으로 이물질을 제거하고 
물로 한번 헹궈주시면 됩니다."

이런 식으로 자연스럽게 이어서 설명해주세요.
""";
        String user = String.format("""
[입력]
process: %s
is_defect: %s
confidence: %s

[근거 청크]
%s

[출력 스키마]
%s
위 정보를 바탕으로 현장 작업자가 쉽게 따라할 수 있도록 친근하고 자연스러운 문체로 조치 방법을 설명해주세요.
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