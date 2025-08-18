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
            normal.setActions(List.of("검사 결과가 정상입니다. 현재 " + req.getProcess() + " 공정은 기준에 적합하게 작동하고 있어서 특별한 조치가 필요하지 않습니다. 정상 운영을 계속 진행하시면 됩니다."));
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

        // 1) 검색
        String query = "공정 1차 점검 기본 조치 절차 안전 확인 공구 부품";
        double[] qvec = embeddingClient.embed(query);
        var chunks = repo.searchByProcess(req.getProcess(), qvec, 8);

        String chunksText = chunks.stream()
                .map(c -> "- (" + c.docName + " " + c.section + " v" + c.version + ")\n" + c.content)
                .collect(Collectors.joining("\n\n"));

        // 2) LLM으로 자연어 텍스트 생성
        RagSuggestResponse resp;
        try {
            // 자연어 텍스트로 조치 방법 생성
            String guidanceText = llmClient.generateText(req, chunksText);

            // 청크에서 공구와 부품 정보 추출 (LLM 응답과 별도로)
            List<String> tools = new ArrayList<>();
            List<String> parts = new ArrayList<>();
            List<String> safetyItems = new ArrayList<>();

            chunks.stream().limit(4).forEach(c -> {
                for (String line : c.content.split("\n")) {
                    var t = line.trim();
                    if (t.startsWith("[공구]")) {
                        tools.addAll(Arrays.stream(t.replace("[공구]","").replace(";",",").split(","))
                                .map(String::trim)
                                .filter(s->!s.isEmpty())
                                .toList());
                    }
                    if (t.startsWith("[부품]")) {
                        parts.addAll(Arrays.stream(t.replace("[부품]","").replace(";",",").split(","))
                                .map(String::trim)
                                .filter(s->!s.isEmpty())
                                .toList());
                    }
                    if (t.startsWith("[안전]")) {
                        safetyItems.addAll(Arrays.stream(t.replace("[안전]","").replace(";",",").split(","))
                                .map(String::trim)
                                .filter(s->!s.isEmpty())
                                .toList());
                    }
                }
            });

            // 기본 안전사항 추가
            if (safetyItems.isEmpty()) {
                safetyItems.addAll(List.of("작업 전 안전장비 착용 필수", "전원 차단 후 작업 진행"));
            }

            resp = new RagSuggestResponse();
            resp.setLevel("TRIAGE");
            resp.setTitle(req.getProcess() + " 공정 조치 안내");

            // 자연어 텍스트를 메인 action으로 설정
            resp.setActions(List.of(guidanceText));

            resp.setTools(new ArrayList<>(new LinkedHashSet<>(tools))); // 중복 제거
            resp.setParts(new ArrayList<>(new LinkedHashSet<>(parts))); // 중복 제거
            resp.setTimeMin(estimateWorkTime(req.getProcess(), guidanceText));
            resp.setVerification(List.of("조치 완료 후 정상 작동 여부 확인", "기준값 범위 내 측정값 확인"));
            resp.setSafety(new ArrayList<>(new LinkedHashSet<>(safetyItems)));

            var sources = chunks.stream().limit(3)
                    .map(c -> Map.of("doc", c.docName, "section", c.section, "version", c.version))
                    .collect(Collectors.toList());
            resp.setSources(new ArrayList<>());
            resp.setOverallConfidence(0.8);
            resp.setNeedHumanReview(req.getConfidence() < 0.7); // 신뢰도가 낮으면 리뷰 필요

        } catch (Exception ex) {
            System.err.println("LLM 텍스트 생성 실패: " + ex.getMessage());

            // 폴백 로직 - 자연어 형태로 개선
            List<String> ruleBasedActions = new ArrayList<>();
            Set<String> tools = new LinkedHashSet<>();
            Set<String> parts = new LinkedHashSet<>();
            Set<String> safetyItems = new LinkedHashSet<>();

            chunks.stream().limit(4).forEach(c -> {
                for (String line : c.content.split("\n")) {
                    var t = line.trim();
                    if (t.startsWith("[조치]")) {
                        ruleBasedActions.add(t.replace("[조치]","").trim());
                    }
                    if (t.startsWith("[공구]")) {
                        tools.addAll(Arrays.stream(t.replace("[공구]","").replace(";",",").split(","))
                                .map(String::trim).filter(s->!s.isEmpty()).toList());
                    }
                    if (t.startsWith("[부품]")) {
                        parts.addAll(Arrays.stream(t.replace("[부품]","").replace(";",",").split(","))
                                .map(String::trim).filter(s->!s.isEmpty()).toList());
                    }
                    if (t.startsWith("[안전]")) {
                        safetyItems.addAll(Arrays.stream(t.replace("[안전]","").replace(";",",").split(","))
                                .map(String::trim).filter(s->!s.isEmpty()).toList());
                    }
                }
            });

            // 폴백 시 자연어 안내문 생성
            String fallbackGuidance = createFallbackGuidance(req.getProcess(), ruleBasedActions, tools, parts);

            resp = new RagSuggestResponse();
            resp.setLevel("TRIAGE");
            resp.setTitle(req.getProcess() + " 공정 1차 점검");
            resp.setActions(List.of(fallbackGuidance));
            resp.setTools(new ArrayList<>(tools));
            resp.setParts(new ArrayList<>(parts));
            resp.setTimeMin(15); // 기본 시간
            resp.setVerification(List.of("조치 완료 후 기준 충족 여부 확인"));
            resp.setSafety(safetyItems.isEmpty() ?
                    List.of("안전수칙 준수", "작업 전 안전장비 착용") :
                    new ArrayList<>(safetyItems));

            var sources = chunks.stream().limit(3)
                    .map(c -> Map.of("doc", c.docName, "section", c.section, "version", c.version))
                    .collect(Collectors.toList());
            resp.setSources(new ArrayList<>());
            resp.setOverallConfidence(0.6);
            resp.setNeedHumanReview(true); // 폴백일 때는 리뷰 필요
        }

        resp.setInspectionId(req.getInspectionId());
        return resp;
    }

    /**
     * 작업 시간 추정
     */
    private int estimateWorkTime(String process, String guidance) {
        int baseTime = 10; // 기본 10분

        // 텍스트 길이 기반 시간 추정
        int textLength = guidance.length();
        if (textLength > 500) baseTime += 10;
        if (textLength > 1000) baseTime += 10;

        // 공정별 추가 시간
        if (process.toLowerCase().contains("engine") || process.toLowerCase().contains("엔진")) {
            baseTime += 15;
        }
        if (process.toLowerCase().contains("brake") || process.toLowerCase().contains("브레이크")) {
            baseTime += 10;
        }

        return Math.min(baseTime, 60); // 최대 60분
    }

    /**
     * 폴백용 자연어 안내문 생성
     */
    private String createFallbackGuidance(String process, List<String> actions, Set<String> tools, Set<String> parts) {
        StringBuilder guidance = new StringBuilder();

        guidance.append(String.format("%s 공정에서 문제가 발생했습니다. ", process));

        if (!actions.isEmpty()) {
            guidance.append("우선 ");
            for (int i = 0; i < actions.size(); i++) {
                if (i > 0) guidance.append(" 그 다음에는 ");
                String cleanAction = actions.get(i)
                        .replaceAll("^\\d+\\)\\s*", "")  // 1) 제거
                        .replaceAll("^[a-zA-Z]\\)\\s*", "")  // a) 제거
                        .trim();

                guidance.append(cleanAction);
                if (i < actions.size() - 1) guidance.append("를 진행하고,");
                else guidance.append("를 수행해주세요. ");
            }
        } else {
            guidance.append("문서에 따라 해당 공정의 표준 점검 절차를 수행해주세요. ");
            guidance.append("불량 원인을 파악한 후 적절한 조치를 취하시기 바랍니다. ");
        }

        if (!tools.isEmpty()) {
            guidance.append("이 작업에는 ");
            guidance.append(String.join(", ", tools));
            guidance.append(" 등의 공구가 필요합니다. ");
        }

        if (!parts.isEmpty()) {
            guidance.append("필요한 부품으로는 ");
            guidance.append(String.join(", ", parts));
            guidance.append(" 등이 있습니다. ");
        }

        guidance.append("작업 전에는 반드시 안전장비를 착용하고 전원을 차단한 후 진행해주세요. ");
        guidance.append("작업이 완료되면 해당 공정이 정상적으로 작동하는지 꼭 확인해주시기 바랍니다.");

        return guidance.toString();
    }
}
