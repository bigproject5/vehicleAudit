package aivle.project.vehicleAudit.domain;

import aivle.project.vehicleAudit.domain.enumerate.InspectionStatus;
import aivle.project.vehicleAudit.domain.enumerate.InspectionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inspection")
@Getter
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

    @Column(name = "solution", length = 500)
    private String aiSuggestion;
}
