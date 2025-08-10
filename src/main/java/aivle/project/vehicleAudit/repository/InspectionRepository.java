package aivle.project.vehicleAudit.repository;

import aivle.project.vehicleAudit.domain.Inspection;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import java.util.List;

public interface InspectionRepository extends JpaRepository<Inspection, Long>, QuerydslPredicateExecutor<Inspection> {

    List<Inspection> findByAuditId(Long auditId);
    Page<Inspection> findByTaskWorkerId(Long workerId, Pageable pageable);

    @EntityGraph(attributePaths = {"task"})
    Optional<Inspection> findWithTaskById(Long inspectionId);
}