package aivle.project.vehicleAudit.repository;

import aivle.project.vehicleAudit.domain.Audit;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRepository extends JpaRepository<Audit, Long> {
    @EntityGraph(attributePaths = {"inspections"})
    Optional<Audit> findWithInspectionsById(Long id);
}

