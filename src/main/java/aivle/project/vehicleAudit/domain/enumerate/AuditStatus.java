package aivle.project.vehicleAudit.domain.enumerate;

import lombok.Getter;

@Getter
public enum AuditStatus {
    IN_DIAGNOSIS("진단 중"),
    IN_PROGRESS("처리 중"),
    COMPLETED("완료");

    private final String description;

    AuditStatus(String description) {
        this.description = description;
    }

}
