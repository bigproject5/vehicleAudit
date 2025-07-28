package aivle.project.vehicleAudit.repository;

import aivle.project.vehicleAudit.domain.Audit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<Audit, Long> {

}