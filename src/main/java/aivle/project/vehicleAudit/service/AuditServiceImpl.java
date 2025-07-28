package aivle.project.vehicleAudit.service;

import aivle.project.vehicleAudit.domain.*;
import aivle.project.vehicleAudit.domain.enumerate.InspectionStatus;
import aivle.project.vehicleAudit.repository.AuditRepository;
import aivle.project.vehicleAudit.repository.InspectionRepository;
import aivle.project.vehicleAudit.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditServiceImpl implements AuditService {


    private final AuditRepository auditRepository;
    private final InspectionRepository inspectionRepository;
    private final TaskRepository taskRepository;

    // 생성자 주입 방식
    @Autowired
    public AuditServiceImpl(
            AuditRepository auditRepository,
            InspectionRepository inspectionRepository,
            TaskRepository taskRepository
    ) {
        this.auditRepository = auditRepository;
        this.inspectionRepository = inspectionRepository;
        this.taskRepository = taskRepository;
    }
    @Override
    public Audit create(Audit audit) {
        return auditRepository.save(audit);
    }
    @Override
    public Page<Audit> findAll(Pageable pageable) {
        return auditRepository.findAll(pageable);
    }

    @Override
    public Audit findById(Long id) {
        return auditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 Audit이 없습니다."));
    }

    @Override
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
    public Inspection findByInspectionId(Long inspectionId) {
        return inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new RuntimeException("해당 검사(Inspection)가 존재하지 않습니다."));
    }

    // (추가: 다른 인터페이스 메서드들은 필요시 구현)
}
