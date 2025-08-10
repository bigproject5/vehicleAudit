package aivle.project.vehicleAudit.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmbeddingClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.embed-model:text-embedding-3-large}")
    private String embedModel;

    public double[] embed(String text) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI api key not configured");
        }
        WebClient client = webClientBuilder.baseUrl(baseUrl).build();
        Map<String, Object> body = Map.of("model", embedModel, "input", text);
        Map<?,?> resp = client.post()
                .uri("/embeddings")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(h -> h.setBearerAuth(apiKey))
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        List<?> data = (List<?>) resp.get("data");
        Object emb = ((Map<?,?>) data.get(0)).get("embedding");
        List<Number> vec = (emb instanceof Map) ? (List<Number>) ((Map<?,?>)emb).get("data") : (List<Number>) emb;
        return vec.stream().mapToDouble(Number::doubleValue).toArray();
    }
}
