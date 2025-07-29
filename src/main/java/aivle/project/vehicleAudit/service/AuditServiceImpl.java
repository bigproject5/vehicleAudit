package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.domain.*;
import aivle.project.vehicleAudit.domain.enumerate.InspectionStatus;
import aivle.project.vehicleAudit.repository.AuditRepository;
import aivle.project.vehicleAudit.repository.InspectionRepository;
import aivle.project.vehicleAudit.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import aivle.project.vehicleAudit.domain.Inspection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditRepository auditRepository;
    private final InspectionRepository inspectionRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public Audit create(Audit audit) {
        return auditRepository.save(audit);  // createdAt 직접 안 건드림
    }


    @Override
    @Transactional
    public Page<Audit> findAll(Pageable pageable) {
        return auditRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public Audit findById(Long id) {
        return auditRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 Audit ID를 찾을 수 없습니다: " + id));
    }

    @Override
    @Transactional
    public List<Inspection> findInspectionsByAuditId(Long auditId) {
        Audit audit = auditRepository.findById(auditId)
                .orElseThrow(() -> new RuntimeException("해당 차량 검사가 존재하지 않습니다."));
        return audit.getInspections();
    }

    @Override
    @Transactional
    public Inspection startTaskOnInspection(Long inspectionId, Long workerId, String workerName) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("해당 검사(Inspection)가 존재하지 않습니다."));

        if (inspection.getStatus() == InspectionStatus.COMPLETED) {
            throw new RuntimeException("이미 완료된 검사입니다.");
        }
        if (inspection.getTask() != null) {
            throw new RuntimeException("이미 작업이 시작된 검사입니다.");
        }

        Task task = new Task();
        task.setWorkerId(workerId);
        task.setWorkerName(workerName);
        task.setStartedAt(LocalDateTime.now());
        taskRepository.save(task);

        inspection.setTask(task);
        inspection.setStatus(InspectionStatus.IN_ACTION);

        return inspectionRepository.save(inspection);
    }

    @Override
    @Transactional
    public Inspection finishTaskOnInspection(Long inspectionId, Long workerId, String workerName) {
        Inspection inspection = inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("해당 검사(Inspection)가 존재하지 않습니다."));

        Task task = inspection.getTask();
        if (task == null) {
            throw new RuntimeException("아직 작업이 시작되지 않은 검사입니다.");
        }
        if (!task.getWorkerId().equals(workerId)) {
            throw new RuntimeException("작업자 정보가 일치하지 않습니다.");
        }
        if (inspection.getStatus() == InspectionStatus.COMPLETED) {
            throw new RuntimeException("이미 완료된 검사입니다.");
        }

        task.setFinishedAt(LocalDateTime.now());
        taskRepository.save(task);

        inspection.setStatus(InspectionStatus.COMPLETED);
        return inspectionRepository.save(inspection);
    }

    @Override
    @Transactional
    public Inspection findByInspectionId(Long inspectionId) {
        return inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("해당 검사(Inspection)가 존재하지 않습니다."));
    }

    // (추가: 다른 인터페이스 메서드들은 필요시 구현)
}
