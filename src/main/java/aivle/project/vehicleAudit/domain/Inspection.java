package aivle.project.vehicleAudit.domain;

import aivle.project.vehicleAudit.domain.enumerate.InspectionStatus;
import aivle.project.vehicleAudit.domain.enumerate.InspectionType;
import aivle.project.vehicleAudit.domain.enumerate.SuggestionLevel;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Entity
@Table(name = "inspection")
@Getter
@Setter
@NoArgsConstructor
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspection_id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "audit_id", nullable = false)
    private Audit audit;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private InspectionType inspectionType;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private InspectionStatus status;

    @Column(name = "is_defect", nullable = false)
    private boolean isDefect;

    @Column(name = "collect_data_path", nullable = false)
    private String collectDataPath;

    @Column(name = "result_data_path")
    private String resultDataPath;

    @Column(name = "solution", length = 300)
    private String aiSuggestion;

    @Enumerated(EnumType.STRING)
    private SuggestionLevel aiSuggestionLevel;

    private Double aiSuggestionConfidence;

    @Lob
    private String aiSuggestionSources;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "task_id")
    private Task task;

    @Column(name = "diagnosis_result")
    private String diagnosisResult;

    @Transient
    private MultipartFile collectDataFile;

    public static Inspection createWithFile(InspectionType inspectionType, MultipartFile collectDataFile) {
        Inspection inspection = new Inspection();
        inspection.setInspectionType(inspectionType);
        inspection.setCollectDataFile(collectDataFile);
        inspection.init();
        return inspection;

    }

    public void init() {
        this.status = InspectionStatus.IN_DIAGNOSIS;
        this.isDefect = false;
        this.resultDataPath = "";
        this.aiSuggestion = "";
        this.diagnosisResult = "";
    }

    public void addToAudit(Audit audit) {
        this.audit = audit;
        audit.addInspection(this);
    }

    public void startTask(Long workerId, String workerName) {
        if (status == InspectionStatus.IN_DIAGNOSIS) {
            throw new RuntimeException("진단 중인 검사입니다. 작업을 시작할 수 없습니다.");
        }
        if (status == InspectionStatus.NORMAL) {
            throw new RuntimeException("이상이 없는 검사입니다. 작업을 시작할 수 없습니다.");
        }
        if (status == InspectionStatus.IN_ACTION) {
            throw new RuntimeException("이미 작업이 진행 중인 검사입니다.");
        }
        if (status == InspectionStatus.COMPLETED) {
            throw new RuntimeException("이미 완료된 검사입니다.");
        }
        Task task = new Task();
        task.allocateWorker(workerId, workerName);
        this.task = task;
        this.status = InspectionStatus.IN_ACTION;
    }

    public void finishTask(long workerId) {
        if (status == InspectionStatus.COMPLETED) {
            throw new RuntimeException("이미 완료된 검사입니다.");
        }
        if (status != InspectionStatus.IN_ACTION) {
            throw new RuntimeException("작업이 진행 중이지 않습니다. 작업을 완료할 수 없습니다.");
        }
        task.done(workerId);
        this.status = InspectionStatus.COMPLETED;
    }

    public void modifyResolve(Long workerId, String resolve) {
        if (status == InspectionStatus.COMPLETED) {
            throw new RuntimeException("이미 완료된 검사입니다. 작업을 수정할 수 없습니다.");
        }
        if (status != InspectionStatus.IN_ACTION) {
            throw new RuntimeException("작업이 진행 중이지 않습니다. 작업을 수정할 수 없습니다.");
        }
        task.modifyResolve(workerId, resolve);
    }

    public boolean isCompleted() {
        return this.status == InspectionStatus.COMPLETED || this.status == InspectionStatus.NORMAL;
    }
}
