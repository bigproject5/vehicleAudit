package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.rest.dto.RagSuggestRequest;
import aivle.project.vehicleAudit.rest.dto.RagSuggestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class RagClient {

    private final WebClient ragWebClient;

    public RagSuggestResponse suggest(RagSuggestRequest req) {
        return ragWebClient.post()
                .uri("/suggest")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(RagSuggestResponse.class)
                .block();
    }
}
