package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.domain.*;
import aivle.project.vehicleAudit.repository.AuditRepository;
import aivle.project.vehicleAudit.repository.InspectionRepository;
import aivle.project.vehicleAudit.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aivle.project.vehicleAudit.domain.Inspection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;
    private final InspectionRepository inspectionRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public Audit create(Audit audit, List<Inspection> inspections) {
        audit.init();
        inspections.forEach(inspection -> {
            inspection.init();
            inspection.addToAudit(audit);  // Inspection에 Audit 설정
        });
        return auditRepository.save(audit);
    }


    @Override
    @Transactional
    public Page<Audit> findAll(Pageable pageable) {
        return auditRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Audit findById(Long id) {
        return auditRepository.findWithInspectionsById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 Audit ID를 찾을 수 없습니다: " + id));
    }

    @Override
    @Transactional
    public Page<Inspection> findInspectionsByWorkerId(Long workerId, Pageable pageable) {
        return inspectionRepository.findByTaskWorkerId(workerId, pageable);
    }

    @Override
    @Transactional
    public Inspection startTaskOnInspection(Long inspectionId, Long workerId, String workerName) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("해당 검사(Inspection)가 존재하지 않습니다."));

        inspection.startTask(workerId, workerName);

        return inspectionRepository.save(inspection);
    }

    @Override
    @Transactional
    public Inspection finishTaskOnInspection(Long inspectionId, Long workerId, String workerName) {
        Inspection inspection = inspectionRepository.findWithTaskById(inspectionId)
                .orElseThrow(() -> new RuntimeException("해당 검사(Inspection)가 존재하지 않습니다."));
        inspection.finishTask(workerId);
        return inspectionRepository.save(inspection);
    }

    @Override
    @Transactional
    public Inspection findByInspectionId(Long inspectionId) {
        return inspectionRepository.findWithTaskById(inspectionId)
                .orElseThrow(() -> new RuntimeException("해당 검사(Inspection)가 존재하지 않습니다."));
    }

    @Override
    @Transactional
    public Inspection updateTaskResolve(Long inspectionId, Long workerId, String resolve) {
        Inspection inspection = inspectionRepository.findWithTaskById(inspectionId)
                .orElseThrow(() -> new RuntimeException("해당 검사(Inspection)가 존재하지 않습니다."));
        inspection.modifyResolve(workerId, resolve);
        return inspection;
    }
}
