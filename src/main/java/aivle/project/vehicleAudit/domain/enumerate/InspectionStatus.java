package aivle.project.vehicleAudit.domain.enumerate;

public enum InspectionStatus {
    IN_DIAGNOSIS, // 진단 중
    NORMAL, // 정상
    ABNORMAL, // 이상
    IN_ACTION, // 조치 중
    COMPLETED; // 완료
}