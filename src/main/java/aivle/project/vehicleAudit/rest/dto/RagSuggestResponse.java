package aivle.project.vehicleAudit.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RagSuggestResponse {
    private String level;                 // TRIAGE | SPECIFIC
    private String title;
    private List<String> actions;
    private List<String> tools;
    private List<String> parts;

    @JsonProperty("time_min")
    private Integer timeMin;

    private List<String> verification;
    private List<String> safety;
    private List<Map<String, String>> sources;

    @JsonProperty("overall_confidence")
    private Double overallConfidence;

    @JsonProperty("need_human_review")
    private Boolean needHumanReview;

    @JsonProperty("inspection_id")
    private Long inspectionId;
}
