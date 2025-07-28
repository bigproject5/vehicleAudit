package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.domain.Audit;
import aivle.project.vehicleAudit.domain.Inspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditService {
    Audit create(Audit audit);
    Page<Audit> findAll(Pageable pageable);
    Audit findById(Long id);
    List<Inspection> findInspectionsByAuditId(Long auditId);
    Inspection startTaskOnInspection(Long inspectionId, Long workerId, String workerName);
    Inspection finishTaskOnInspection(Long inspectionId, Long workerId, String workerName);
    Inspection findByInspectionId(Long inspectionId);
}