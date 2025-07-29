package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.domain.Audit;
import aivle.project.vehicleAudit.domain.Inspection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditService {
    Audit create(Audit audit, List<Inspection> inspections);
    Page<Audit> findAll(Pageable pageable);
    Audit findById(Long id);
    Page<Inspection> findInspectionsByWorkerId(Long workerId, Pageable pageable);
    Inspection startTaskOnInspection(Long inspectionId, Long workerId, String workerName);
    Inspection finishTaskOnInspection(Long inspectionId, Long workerId, String workerName);
    Inspection findByInspectionId(Long inspectionId);
    Inspection updateTaskResolve(Long inspectionId, Long workerId, String resolve);
}