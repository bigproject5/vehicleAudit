package aivle.project.vehicleAudit.rest.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RagSuggestRequest {
    private String process; // WIPER/WASHER/LAMP/PAINT/ENGINE

    @JsonProperty("is_defect")
    private boolean isDefect;

    private Double confidence;

    @JsonProperty("model_version")
    private String modelVersion;

    @JsonProperty("inspection_id")
    private Long inspectionId;

    @JsonProperty("vehicle_model")
    private String vehicleModel;
}
