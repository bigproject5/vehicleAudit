package aivle.project.vehicleAudit.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // Exclude null values from JSON serialization
public class ResponseDTO<T> {

    private final String code;
    private final T data;
    private final String message;

    private ResponseDTO(String code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static <T> ResponseDTO<T> success(String code, T data) {
        return new ResponseDTO<>("SUCCESS", data, null);
    }

    public static <T> ResponseDTO<T> success(T data) {
        return new ResponseDTO<>("SUCCESS", data, null);
    }

    public static <T> ResponseDTO<T> success() {
        return new ResponseDTO<>("SUCCESS", null, null);
    }

    public static <T> ResponseDTO<T> error(String code, String message) {
        return new ResponseDTO<>(code, null, message);
    }
}