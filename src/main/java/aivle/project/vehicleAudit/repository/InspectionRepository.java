package aivle.project.vehicleAudit.repository;

import aivle.project.vehicleAudit.domain.Inspection;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    List<Inspection> findByAuditId(Long auditId);
}
