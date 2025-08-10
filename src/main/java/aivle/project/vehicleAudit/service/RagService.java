package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.rest.dto.RagSuggestRequest;
import aivle.project.vehicleAudit.rest.dto.RagSuggestResponse;
import aivle.project.vehicleAudit.repository.GuideChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RagService {

    private final GuideChunkRepository repo;
    private final EmbeddingClient embeddingClient;
    private final LlmClient llmClient;

    public RagSuggestResponse suggest(RagSuggestRequest req) {
        if (!req.isDefect()) {
            RagSuggestResponse normal = new RagSuggestResponse();
            normal.setLevel("TRIAGE");
            normal.setTitle("정상 판정");
            normal.setActions(Collections.emptyList());
            normal.setTools(Collections.emptyList());
            normal.setParts(Collections.emptyList());
            normal.setTimeMin(0);
            normal.setVerification(List.of("특이사항 없음"));
            normal.setSafety(Collections.emptyList());
            normal.setSources(new ArrayList<>());
            normal.setOverallConfidence(1.0);
            normal.setNeedHumanReview(false);
            normal.setInspectionId(req.getInspectionId());
            return normal;
        }

        String query = "공정 1차 점검 기본 조치 절차 안전 확인 공구 부품";
        double[] qvec = embeddingClient.embed(query);
        var chunks = repo.searchByProcess(req.getProcess(), qvec, 8);

        String chunksText = chunks.stream()
                .map(c -> "- (" + c.docName + " " + c.section + " v" + c.version + ")\n" + c.content)
                .collect(Collectors.joining("\n\n"));

        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("level", "TRIAGE");
        schema.put("title", "string");
        schema.put("actions", List.of("string"));
        schema.put("tools", List.of("string"));
        schema.put("parts", List.of("string"));
        schema.put("time_min", 0);
        schema.put("verification", List.of("string"));
        schema.put("safety", List.of("string"));
        schema.put("sources", List.of(Map.of("doc","string","section","string","version","string")));
        schema.put("overall_confidence", 0.0);
        schema.put("need_human_review", false);

        RagSuggestResponse resp;
        try {
            resp = llmClient.generate(req, chunksText, schema);
        } catch (Exception ex) {
            // fallback
            List<String> actions = new ArrayList<>();
            Set<String> tools = new LinkedHashSet<>();
            Set<String> parts = new LinkedHashSet<>();
            chunks.stream().limit(4).forEach(c -> {
                for (String line : c.content.split("\n")) {
                    var t = line.trim();
                    if (t.startsWith("[조치]")) actions.add(t.replace("[조치]","").trim());
                    if (t.startsWith("[공구]")) tools.addAll(Arrays.stream(t.replace("[공구]","").replace(";",",").split(",")).map(String::trim).filter(s->!s.isEmpty()).toList());
                    if (t.startsWith("[부품]")) parts.addAll(Arrays.stream(t.replace("[부품]","").replace(";",",").split(",")).map(String::trim).filter(s->!s.isEmpty()).toList());
                }
            });
            resp = new RagSuggestResponse();
            resp.setLevel("TRIAGE");
            resp.setTitle(req.getProcess() + " 공정 1차 점검(트리아지)");
            resp.setActions(actions.isEmpty()? List.of("문서 근거를 바탕으로 1차 점검을 수행하세요.") : actions);
            resp.setTools(new ArrayList<>(tools));
            resp.setParts(new ArrayList<>(parts));
            resp.setTimeMin(10);
            resp.setVerification(List.of("기준 충족 확인"));
            resp.setSafety(List.of("안전수칙 준수"));
            var src = chunks.stream().limit(3).map(c -> Map.of("doc", c.docName, "section", c.section, "version", c.version)).toList();
            resp.setSources(new ArrayList<>(src));
            resp.setOverallConfidence(0.5);
            resp.setNeedHumanReview(true);
        }
        resp.setInspectionId(req.getInspectionId());
        return resp;
    }
}
